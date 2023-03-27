package fi.nls.dbquality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.*;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import fi.nls.dbquality.model.*;

class QualityServiceTest {

    private final String BAD_QUERY_ERROR_MSG = "bad SQL grammar";

    @Mock
    private RuleExecutorService workRuleExecutorService;
    @Mock
    private DataSource dataSource;

    QualityService qualityService;

    private AutoCloseable closeable;

    @BeforeEach
    public void beforeEach() {
        closeable = MockitoAnnotations.openMocks(this);
        qualityService = new QualityService("id_field");

        Util.setField("workRuleExecutorService", QualityService.class, qualityService, workRuleExecutorService);
    }

    @AfterEach
    public void afterEach() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("Rule is executed with ids of category")
    void rulesAreExecutedWithIds() {
        var rules = List.of(
                createQualityRule("ruleId1", "sql1"),
                createQualityRule("ruleId2", "sql2")
        );
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> qualityCriteria = createCriteria(List.of(id1));
        qualityCriteria.addAll(createCriteria(List.of(id2)));

        qualityService.executeRules(dataSource, qualityCriteria, rules);

        verify(workRuleExecutorService, times(2)).executeRule(any(JdbcTemplate.class), any(), eq(qualityCriteria));
    }

    @Test
    @DisplayName("Rule results are returned")
    void ruleResultsAreReturned() {
        var rules = List.of(
                createQualityRule("ruleId1", null)
        );
        UUID id1 = UUID.randomUUID();
        UUID id11 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id22 = UUID.randomUUID();
        when(workRuleExecutorService.executeRule(any(JdbcTemplate.class), any(), any())).thenReturn(List.of(
                createQueryResult(id1, id11),
                createQueryResult(id2, id22)
        ));

        List<UUID> qualityCriteria = createCriteria(List.of(id1, id2));
        QualityRunResult qualityRunResult = qualityService.executeRules(dataSource, qualityCriteria, rules);

        // Test executed rules
        Map<String, Set<UUID>> executedRules = qualityRunResult.getExecutedRulesById();
        assertEquals(1, executedRules.size());
        Set<UUID> ids = executedRules.get("ruleId1");
        assertThat(ids).containsAll(List.of(id1, id2));

        // Test results
        List<QualityResult> results = qualityRunResult.getQualityResults();
        assertEquals(2, results.size());
        QualityResult result1 = results.get(0);
        assertEquals(id1, result1.getTargetId());
        assertEquals(id11, result1.getRelatedId());
        assertEquals("ruleId1", result1.getId());
        QualityResult result2 = results.get(1);
        assertEquals(id2, result2.getTargetId());
        assertEquals(id22, result2.getRelatedId());
        assertEquals("ruleId1", result2.getId());
    }

    @Test
    @DisplayName("Rules are rerun individually for each id when bad query result is returned")
    void rulesAreRerun() {
        QualityRule rule = createQualityRule("ruleId1", null);
        var rules = List.of(rule);
        UUID id1 = UUID.randomUUID();
        UUID id11 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(workRuleExecutorService.executeRule(any(JdbcTemplate.class), any(), any()))
                .thenReturn(List.of(
                        createBadQueryResult(id1),
                        createBadQueryResult(id2)
                ))
                .thenReturn(List.of(createQueryResult(id1, id11)))
                .thenReturn(List.of(createBadQueryResult(id2)));
        List<UUID> ids = List.of(id1, id2);
        List<UUID> qualityCriteria = createCriteria(ids);

        List<QualityResult> results = qualityService.executeRules(dataSource, qualityCriteria, rules).getQualityResults();

        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), eq(ids));
        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), eq(List.of(id1)));
        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), eq(List.of(id2)));
        assertEquals(2, results.size());
        // First result is individually executed query for id1, query returns QualityQueryResult
        QualityResult result1 = results.get(0);
        assertEquals(id1, result1.getTargetId());
        assertEquals(id11, result1.getRelatedId());
        assertEquals("ruleId1", result1.getId());
        // Second result is individually executed query for id2, query returns BadQueryResult
        QualityResult result2 = results.get(1);
        assertEquals(id2, result2.getTargetId());
        assertEquals("ruleId1", result2.getId());
    }

    @Test
    @DisplayName("Single result is returned when bad query result is received and one id is given")
    void badQueryNoIds() {
        QualityRule rule = createQualityRule("ruleId1", null);
        var rules = List.of(rule);
        when(workRuleExecutorService.executeRule(any(JdbcTemplate.class), any(), any())).thenReturn(List.of(createBadQueryResult(null)));

        List<UUID> qualityCriteria = createCriteria(List.of(UUID.randomUUID()));
        List<QualityResult> results = qualityService.executeRules(dataSource, qualityCriteria, rules).getQualityResults();

        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), any());
        assertEquals(1, results.size());
        BadQueryQualityResult result = (BadQueryQualityResult) results.get(0);
        assertNull(result.getTargetId());
        assertEquals("ruleId1", result.getId());
        assertEquals(BAD_QUERY_ERROR_MSG, result.getErrorMessage());
    }

    private QualityRule createQualityRule(String ruleId, String sql) {
        QualityRule rule = new QualityRule();
        rule.setRuleUniqueId(ruleId);
        rule.setSql(sql);
        return rule;
    }

    private List<UUID> createCriteria(List<UUID> ids) {
        return new ArrayList<>(ids);
    }

    private QualityQueryResult createQueryResult(UUID targetId, UUID relatedId) {
        QualityQueryResult result = new QualityQueryResult();
        result.setTargetId(targetId);
        result.setRelatedId(relatedId);
        return result;
    }

    private BadQueryResult createBadQueryResult(UUID targetId) {
        BadQueryResult result = new BadQueryResult();
        result.setTargetId(targetId);
        result.setErrorMessage(BAD_QUERY_ERROR_MSG);
        return result;
    }
}
