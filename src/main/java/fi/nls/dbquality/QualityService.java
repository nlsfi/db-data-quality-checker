package fi.nls.dbquality;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import fi.nls.dbquality.model.*;

public class QualityService {
    private RuleExecutorService workRuleExecutorService;

    /**
     * Constructor
     *
     * @param idFieldName
     *            id column name
     * @throws IllegalArgumentException
     *             if the id column name is not vali
     */
    public QualityService(String idFieldName) {
        this(new RuleExecutorService(idFieldName));
    }

    protected QualityService(RuleExecutorService workRuleExecutorService) {
        this.workRuleExecutorService = workRuleExecutorService;
    }

    /**
     * Execute quality rules
     *
     * @param dataSource
     * @param features list of ids for source_id_filter placeholder in sql clauses. Empty list: source_id_filter is replaced
     *   with criteria <code>1=1</code>. Null: source_id_filter is replaced with criteria <code>1=0</code>.
     * @param rules rules to be executed
     * @return {@link fi.nls.dbquality.model.QualityRunResult}
     */
    public QualityRunResult executeRules(DataSource dataSource, List<UUID> features, List<? extends QualityRule> rules) {
        var jdbcTemplate = new JdbcTemplate(dataSource);
        Map<String, Set<UUID>> executedRulesById = new HashMap<>();
        List<QualityResult> qualityResults = rules
                .stream()
                .flatMap(rule -> executeRule(jdbcTemplate, rule, features, executedRulesById))
                .collect(Collectors.toList());

        return new QualityRunResult(qualityResults, executedRulesById);
    }

    private Stream<QualityResult> executeRule(JdbcTemplate jdbcTemplate, QualityRule rule, List<UUID> ids, Map<String, Set<UUID>> executedRulesById) {
        List<QualityQueryResult> queryResults = workRuleExecutorService.executeRule(jdbcTemplate, rule.getSql(), ids);
        if (ids != null) {
            Set<UUID> mapIds = executedRulesById.get(rule.getRuleUniqueId());
            if (mapIds != null) {
                mapIds.addAll(ids);
            } else {
                executedRulesById.put(rule.getRuleUniqueId(), new HashSet<>(ids));
            }
        } else {
            executedRulesById.put(rule.getRuleUniqueId(), new HashSet<>());
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
        result.setViolatingGeometry(queryResult.getGeometryError());
        return result;
    }

    private QualityResult mapBadQueryResult(QualityRule rule, BadQueryResult queryResult) {
        QualityResult result = createBaseResult(rule);
        result.setTargetId(queryResult.getTargetId());
        return result;
    }

    private QualityResult createBaseResult(QualityRule rule) {
        QualityResult result = new QualityResult();
        result.setId(rule.getRuleUniqueId());
        return result;
    }

}
