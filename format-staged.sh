#!/bin/sh
set -e

function printInfoMessage() {
  printf -- "-%.0s" {1..50}
  printf "\n$1\n"
  printf -- '-%.0s' {1..50}
  printf "\n"
}

function printWarningMessage() {
  printf "*%.0s" {1..70}
  printf "\n$1\n"
  printf '*%.0s' {1..70}
  printf "\n"
}

if [[ ! $(git diff --name-only --cached) ]]; then
  printInfoMessage "No changes added to commit!"
  exit
fi

diffBeforeFormatter=$(git --no-pager diff | md5sum)

printInfoMessage "Running mvn spotless:apply for staged files!"

filesForSpotless=""
for line in $(git diff --name-only --cached)
do
  filePath=${line////\\\\}
  filesForSpotless+=".*$filePath,"
done

mvn spotless:apply -DspotlessFiles="$filesForSpotless"


printInfoMessage "Running mvn checkstyle:check for staged files!"
classesForCheckstyle=""
for line in $(git diff --name-only --cached)
do
  IFS=/ read -a splitted <<< $line
  classesForCheckstyle+="**\/${splitted[-1]},"
done

mvn checkstyle:check -Dcheckstyle.includes="$classesForCheckstyle"

if [[ $diffBeforeFormatter != $(git --no-pager diff | md5sum) ]]; then
  printf "\n\n"
  printWarningMessage "Formatter changes applied, please stage changes before commit"
  exit 1
fi
