# Development instructions

## Development environment setup

- install Java and Maven
    - [JDK](https://openjdk.org/)
    - [Maven (>=3.6)](https://maven.apache.org/download.cgi)

- for Eclipse, install .editorconfig [plugin](https://marketplace.eclipse.org/content/editorconfig-eclipse)
- install pre-commit hook
  - windows: `xcopy format-staged.sh .git\hooks\pre-commit /Y`
  - linux: `cp format-staged.sh .git/hooks/pre-commit`

## Commit message style

Commit messages should follow [Conventional Commits notation](https://www.conventionalcommits.org/en/v1.0.0/#summary).

## Code style

Code style is checked against checkstyle configuration based on Google styles. Run checkstyle with:
```
mvn checkstyle:check
```

Run automated code formatting with command:
```
mvn spotless:apply
```

## Release steps

TODO

