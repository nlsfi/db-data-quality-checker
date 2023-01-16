/*  
  Copyright (C) 2022 National Land Survey of Finland
  (https://www.maanmittauslaitos.fi/en).


  This file is part of quality-service.

  quality-service is free software: you can redistribute it and/or
  modify it under the terms of the GNU General Public License as published
  by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  quality-service is distributed in the hope that it will be
  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with quality-service. If not, see <https://www.gnu.org/licenses/>.
*/
package fi.nls.quality;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import fi.nls.quality.model.*;

public class QualityService {

    private List<QualityRule> allQualityRules;
    private List<QualityRule> rulesWithQGisExpression;

    private RuleExecutorService workRuleExecutorService;
    private QualityRuleFileReader qualityRuleFileReader;

    public QualityService(String ruleFile, String idFieldName) {
        this(new RuleExecutorService(idFieldName), new QualityRuleFileReader(ruleFile));
    }

    protected QualityService(RuleExecutorService workRuleExecutorService, QualityRuleFileReader qualityRuleFileReader) {
        this.workRuleExecutorService = workRuleExecutorService;
        this.qualityRuleFileReader = qualityRuleFileReader;
    }

    public List<QualityRule> getAllQualityRules() {
        readRules();
        return allQualityRules;
    }

    public List<QualityRule> getRulesWithQGisExpression() {
        readRules();
        return rulesWithQGisExpression;
    }

    private void readRules() {
        if (allQualityRules != null) {
            return;
        }
        QualityRules rules = qualityRuleFileReader.readRules();
        if (rules.getQualityRules() != null) {
            allQualityRules = rules.getQualityRules();
            rulesWithQGisExpression = rules.getQualityRules()
                    .stream()
                    .filter(rule -> Objects.nonNull(rule.getQgisExpression()))
                    .collect(Collectors.toList());
        } else {
            allQualityRules = Collections.emptyList();
            rulesWithQGisExpression = Collections.emptyList();
        }
    }

    public QualityRunResult executeRules(DataSource dataSource, Map<String, List<UUID>> featuresByCategory) {
        Objects.requireNonNull(featuresByCategory, "featuresByCategory is required");
        Set<String> categories = featuresByCategory.keySet();
        List<QualityRule> rules = getAllQualityRules()
                .stream()
                .filter(rule -> categories.contains(rule.getCategory()))
                .collect(Collectors.toList());

        return executeRules(dataSource, featuresByCategory, rules);
    }

    public QualityRunResult executeRulesIfRelatedCategoryChanged(DataSource dataSource, Set<String> categoriesToCheck) {
        Objects.requireNonNull(categoriesToCheck, "categoriesToCheck is required");
        List<QualityRule> rules = getAllQualityRules()
                .stream()
                .filter(rule -> Objects.nonNull(rule.getCheckIfChanged()))
                .filter(rule -> rule.getCheckIfChanged().stream().anyMatch(cat -> categoriesToCheck.contains(cat)))
                .collect(Collectors.toList());

        return executeRules(dataSource, Collections.emptyMap(), rules);
    }

    private QualityRunResult executeRules(DataSource dataSource, Map<String, List<UUID>> featuresByCategory, List<QualityRule> rules) {
        var jdbcTemplate = new JdbcTemplate(dataSource);
        Map<String, Set<UUID>> executedRulesById = new HashMap<>();
        List<QualityResult> qualityResults = rules
                .stream()
                .flatMap(rule -> executeRule(jdbcTemplate, rule, featuresByCategory.get(rule.getCategory()), executedRulesById))
                .collect(Collectors.toList());

        return new QualityRunResult(qualityResults, executedRulesById);
    }

    private Stream<QualityResult> executeRule(JdbcTemplate jdbcTemplate, QualityRule rule, List<UUID> ids, Map<String, Set<UUID>> executedRulesById) {
        List<QualityQueryResult> queryResults = workRuleExecutorService.executeRule(jdbcTemplate, rule.getSql(), ids);
        if (ids != null) {
            Set<UUID> mapIds = executedRulesById.get(rule.getRuleId());
            if (mapIds != null) {
                mapIds.addAll(ids);
            } else {
                executedRulesById.put(rule.getRuleId(), new HashSet<>(ids));
            }
        } else {
            executedRulesById.put(rule.getRuleId(), new HashSet<>());
        }
        if (!queryResults.isEmpty() && queryResults.get(0) instanceof BadQueryResult && ids != null && ids.size() > 1) {
            return executeIndividualQueries(jdbcTemplate, rule, ids);
        }
        return queryResults.stream().map(
                queryResult -> mapQueryResult(rule, queryResult)
        );
    }

    private Stream<QualityResult> executeIndividualQueries(JdbcTemplate jdbcTemplate, QualityRule rule, List<UUID> ids) {
        AtomicReference<Stream<QualityResult>> resultStream = new AtomicReference<>(Stream.empty());
        ids.forEach(id -> {
            List<QualityQueryResult> individualQueryResults = workRuleExecutorService.executeRule(jdbcTemplate, rule.getSql(), List.of(id));
            resultStream.set(Stream.concat(resultStream.get(), individualQueryResults.stream().map(queryResult -> mapQueryResult(rule, queryResult))));

        });
        return resultStream.get();
    }

    private QualityResult mapQueryResult(QualityRule rule, QualityQueryResult queryResult) {
        if (queryResult instanceof BadQueryResult) {
            return mapBadQueryResult(rule, (BadQueryResult) queryResult);
        }
        QualityResult result = createBaseResult(rule);
        result.setTargetId(queryResult.getTargetId());
        result.setRelatedId(queryResult.getRelatedId());
        result.setDescriptions(rule.getDescriptions());
        result.setViolatingGeometry(queryResult.getGeometryError());
        return result;
    }

    private QualityResult mapBadQueryResult(QualityRule rule, BadQueryResult queryResult) {
        QualityResult result = createBaseResult(rule);
        result.setTargetId(queryResult.getTargetId());
        List<Description> descriptions = rule.getDescriptions().stream().map(ruleDescription -> {
            Description description = new Description();
            description.setDescription(queryResult.getErrorMessage());
            description.setLang(ruleDescription.getLang());
            return description;
        }).collect(Collectors.toList());
        result.setDescriptions(descriptions);
        return result;
    }

    private QualityResult createBaseResult(QualityRule rule) {
        QualityResult result = new QualityResult();
        result.setCategory(rule.getCategory());
        result.setRelatedCategory(rule.getTarget());
        result.setRuleId(rule.getRuleId());
        result.setType(rule.getType());
        result.setPriority(rule.getPriority());
        result.setViolatingAttributeName(rule.getAttributeName());
        return result;
    }

    public void clearCache() {
        allQualityRules = null;
        rulesWithQGisExpression = null;
    }
}
