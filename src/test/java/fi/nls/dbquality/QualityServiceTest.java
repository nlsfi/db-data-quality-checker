package fi.nls.dbquality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

    @Mock
    private RuleExecutorService workRuleExecutorService;
    @Mock
    private QualityRuleFileReader qualityRuleFileReader;
    @Mock
    private DataSource dataSource;

    QualityService qualityService;

    private AutoCloseable closeable;

    @BeforeEach
    public void beforeEach() {
        closeable = MockitoAnnotations.openMocks(this);
        qualityService = new QualityService("mock-filename", "id_field");

        Util.setField("qualityRuleFileReader", QualityService.class, qualityService, qualityRuleFileReader);
        Util.setField("workRuleExecutorService", QualityService.class, qualityService, workRuleExecutorService);
    }

    @AfterEach
    public void afterEach() throws Exception {
        closeable.close();
    }

    @Test
    void getAllRulesReturnsEmptyListIfNoRules() {
        when(qualityRuleFileReader.readRules()).thenReturn(new QualityRules());
        List<QualityRule> allRules = qualityService.getAllQualityRules();
        assertTrue(allRules.isEmpty());
    }

    @Test
    void getRulesWithQGisExpressionReturnsEmptyListIfNoRules() {
        when(qualityRuleFileReader.readRules()).thenReturn(new QualityRules());
        List<QualityRule> qgisRules = qualityService.getRulesWithQGisExpression();
        assertTrue(qgisRules.isEmpty());
    }

    @Test
    void getRulesWithQGisExpressionReturnsEmptyListIfNoQgisRules() {
        QualityRules qualityRules = createQualityRules(
                createQualityRule("ruleId1", null)
        );
        when(qualityRuleFileReader.readRules()).thenReturn(qualityRules);

        List<QualityRule> qgisRules = qualityService.getRulesWithQGisExpression();

        assertTrue(qgisRules.isEmpty());
    }

    @Test
    void getAllRulesReturnsAllRulesWhenNoQgisRules() {
        QualityRules qualityRules = createQualityRules(
                createQualityRule("ruleId1", null)
        );
        when(qualityRuleFileReader.readRules()).thenReturn(qualityRules);

        List<QualityRule> allRules = qualityService.getAllQualityRules();

        assertEquals(1, allRules.size());
        assertEquals("ruleId1", allRules.get(0).getRuleId());
    }

    @Test
    void getRulesWithQGisExpressionReturnsOnlyQgisRules() {
        QualityRules qualityRules = createQualityRules(
                createQualityRule("ruleId1", null),
                createQualityRule("ruleId2", "qgis_test_expression1"),
                createQualityRule("ruleId3", "qgis_test_expression2"),
                createQualityRule("ruleId4", null)
        );
        when(qualityRuleFileReader.readRules()).thenReturn(qualityRules);

        List<QualityRule> qgisRules = qualityService.getRulesWithQGisExpression();

        assertEquals(2, qgisRules.size());
        assertEquals("ruleId2", qgisRules.get(0).getRuleId());
        assertEquals("ruleId3", qgisRules.get(1).getRuleId());
    }

    @Test
    void getAllRulesReturnsAllRules() {
        QualityRules qualityRules = createQualityRules(
                createQualityRule("ruleId1", null),
                createQualityRule("ruleId2", "qgis_test_expression1"),
                createQualityRule("ruleId3", "qgis_test_expression2"),
                createQualityRule("ruleId4", null)
        );
        when(qualityRuleFileReader.readRules()).thenReturn(qualityRules);

        List<QualityRule> allRules = qualityService.getAllQualityRules();

        assertEquals(4, allRules.size());
        assertEquals("ruleId1", allRules.get(0).getRuleId());
        assertEquals("ruleId2", allRules.get(1).getRuleId());
        assertEquals("ruleId3", allRules.get(2).getRuleId());
        assertEquals("ruleId4", allRules.get(3).getRuleId());
    }

    @Test
    void qualityRuleFileReaderShouldBeCalledOnlyOnce() {
        QualityRules qualityRules = createQualityRules(
                createQualityRule("ruleId1", null)
        );
        when(qualityRuleFileReader.readRules()).thenReturn(qualityRules);

        qualityService.getRulesWithQGisExpression();
        qualityService.getRulesWithQGisExpression();
        qualityService.getRulesWithQGisExpression();
        verify(qualityRuleFileReader, times(1)).readRules();
    }

    @Test
    @DisplayName("Exception is thrown when no criteria is given")
    void exceptionIsThrownWhenNoCriteria() {
        assertThrows(NullPointerException.class, () -> {
            qualityService.executeRules(dataSource, null);
        });
    }

    @Test
    @DisplayName("Rules with a specific category are executed when criteria is given")
    void rulesWithCategoryAreExecuted() {
        when(qualityRuleFileReader.readRules()).thenReturn(createQualityRules(
                createQualityRule("ruleId1", "category1", "sql1", null),
                createQualityRule("ruleId2", "category2", "sql2", null)
        ));
        Map<String, List<UUID>> qualityCriteria = createCriteria("category1", null);

        qualityService.executeRules(dataSource, qualityCriteria);

        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), eq("sql1"), any());
    }

    @Test
    @DisplayName("Rule is executed with ids of category")
    void rulesAreExecutedWithIds() {
        when(qualityRuleFileReader.readRules()).thenReturn(createQualityRules(
                createQualityRule("ruleId1", "category1", "sql1", null),
                createQualityRule("ruleId2", "category2", "sql2", null)
        ));
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Map<String, List<UUID>> qualityCriteria = createCriteria("category1", List.of(id1));
        qualityCriteria.putAll(createCriteria("category2", List.of(id2)));

        qualityService.executeRules(dataSource, qualityCriteria);

        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), eq(qualityCriteria.get("category1")));
        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), eq(qualityCriteria.get("category2")));
    }

    @Test
    @DisplayName("Rule results are returned")
    void ruleResultsAreReturned() {
        when(qualityRuleFileReader.readRules()).thenReturn(createQualityRules(
                createQualityRule("ruleId1", null)
        ));
        UUID id1 = UUID.randomUUID();
        UUID id11 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id22 = UUID.randomUUID();
        when(workRuleExecutorService.executeRule(any(JdbcTemplate.class), any(), any())).thenReturn(List.of(
                createQueryResult(id1, id11),
                createQueryResult(id2, id22)
        ));

        Map<String, List<UUID>> qualityCriteria = createCriteria("category1", List.of(id1, id2));
        QualityRunResult qualityRunResult = qualityService.executeRules(dataSource, qualityCriteria);

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
        assertEquals("ruleId1", result1.getRuleId());
        QualityResult result2 = results.get(1);
        assertEquals(id2, result2.getTargetId());
        assertEquals(id22, result2.getRelatedId());
        assertEquals("ruleId1", result2.getRuleId());
    }

    @Test
    @DisplayName("Rules are rerun individually for each id when bad query result is returned")
    void rulesAreRerun() {
        QualityRule rule = createQualityRule("ruleId1", null);
        when(qualityRuleFileReader.readRules()).thenReturn(createQualityRules(rule));
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
        Map<String, List<UUID>> qualityCriteria = createCriteria("category1", ids);

        List<QualityResult> results = qualityService.executeRules(dataSource, qualityCriteria).getQualityResults();

        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), eq(ids));
        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), eq(List.of(id1)));
        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), eq(List.of(id2)));
        assertEquals(2, results.size());
        // First result is individually executed query for id1, query returns QualityQueryResult
        QualityResult result1 = results.get(0);
        assertEquals(id1, result1.getTargetId());
        assertEquals(id11, result1.getRelatedId());
        assertEquals("ruleId1", result1.getRuleId());
        // Second result is individually executed query for id2, query returns BadQueryResult
        QualityResult result2 = results.get(1);
        assertEquals(id2, result2.getTargetId());
        assertEquals(rule.getDescriptions().size(), result2.getDescriptions().size());
        result2.getDescriptions().forEach(description -> assertEquals("bad SQL grammar", description.getDescription()));
        assertEquals("ruleId1", result2.getRuleId());
    }

    @Test
    @DisplayName("Single result is returned when bad query result is received and one id is given")
    void badQueryNoIds() {
        QualityRule rule = createQualityRule("ruleId1", null);
        when(qualityRuleFileReader.readRules()).thenReturn(createQualityRules(rule));
        when(workRuleExecutorService.executeRule(any(JdbcTemplate.class), any(), any())).thenReturn(List.of(createBadQueryResult(null)));

        Map<String, List<UUID>> qualityCriteria = createCriteria("category1", List.of(UUID.randomUUID()));
        List<QualityResult> results = qualityService.executeRules(dataSource, qualityCriteria).getQualityResults();

        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), any(), any());
        assertEquals(1, results.size());
        QualityResult result = results.get(0);
        assertNull(result.getTargetId());
        assertEquals(rule.getDescriptions().size(), result.getDescriptions().size());
        result.getDescriptions().forEach(description -> assertEquals("bad SQL grammar", description.getDescription()));
        assertEquals("ruleId1", result.getRuleId());
    }

    @Test
    @DisplayName("executeRulesIfRelatedCategoryChanged should not execute rules if no rules with check_if_changed attribute exists")
    void executeRulesIfRelatedCategoryChangedShouldNotExecuteRulesIfNoRulesWithCheckIfChangedAttribute() {
        when(qualityRuleFileReader.readRules()).thenReturn(createQualityRules(
                createQualityRule("id1", "expression1")
        ));

        Set<String> categoriesToCheck = new HashSet<>();
        categoriesToCheck.add("test_category1");
        categoriesToCheck.add("test_category2");
        qualityService.executeRulesIfRelatedCategoryChanged(dataSource, categoriesToCheck);

        verifyNoMoreInteractions(workRuleExecutorService);
    }

    @Test
    @DisplayName("executeRulesIfRelatedCategoryChanged should not execute rules if no matching category found")
    void executeRulesIfRelatedCategoryChangedShouldNotExecuteRulesIfNoMatchingRules() {
        when(qualityRuleFileReader.readRules()).thenReturn(createQualityRules(
                createQualityRuleWithCheckIfChanged("sql_1", "non_matching_category")
        ));

        Set<String> categoriesToCheck = new HashSet<>();
        categoriesToCheck.add("test_category1");
        categoriesToCheck.add("test_category2");
        qualityService.executeRulesIfRelatedCategoryChanged(dataSource, categoriesToCheck);

        verifyNoMoreInteractions(workRuleExecutorService);
    }

    @Test
    @DisplayName("executeRulesIfRelatedCategoryChanged should execute rules only for matching category")
    void executeRulesIfRelatedCategoryChangedShouldExecuteOnlyOneMatchingRule() {
        when(qualityRuleFileReader.readRules()).thenReturn(createQualityRules(
                createQualityRuleWithCheckIfChanged("sql_1", "non_matching_category"),
                createQualityRuleWithCheckIfChanged("sql_matching", "matching_category"),
                createQualityRuleWithCheckIfChanged("sql_2", "non_matching_category")
        ));

        Set<String> categoriesToCheck = new HashSet<>();
        categoriesToCheck.add("test_category1");
        categoriesToCheck.add("matching_category");
        qualityService.executeRulesIfRelatedCategoryChanged(dataSource, categoriesToCheck);

        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), eq("sql_matching"), isNull());
        verifyNoMoreInteractions(workRuleExecutorService);
    }

    @Test
    @DisplayName("executeRulesIfRelatedCategoryChanged should execute rules for all matching categories")
    void executeRulesIfRelatedCategoryChangedShouldExecuteAllMatchingRule() {
        when(qualityRuleFileReader.readRules()).thenReturn(createQualityRules(
                createQualityRuleWithCheckIfChanged("sql_1", "non_matching_category"),
                createQualityRuleWithCheckIfChanged("sql_matching_1", "matching_category_1"),
                createQualityRuleWithCheckIfChanged("sql_matching_2", "matching_category_2"),
                createQualityRuleWithCheckIfChanged("sql_2", "non_matching_category")
        ));
        Set<String> categoriesToCheck = new HashSet<>();
        categoriesToCheck.add("test_category1");
        categoriesToCheck.add("matching_category_1");
        categoriesToCheck.add("matching_category_2");
        qualityService.executeRulesIfRelatedCategoryChanged(dataSource, categoriesToCheck);

        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), eq("sql_matching_1"), isNull());
        verify(workRuleExecutorService, times(1)).executeRule(any(JdbcTemplate.class), eq("sql_matching_2"), isNull());
        verifyNoMoreInteractions(workRuleExecutorService);
    }

    private QualityRules createQualityRules(QualityRule... qualityRuleItems) {
        List<QualityRule> qualityRuleList = List.of(qualityRuleItems);
        QualityRules qualityRules = new QualityRules();
        qualityRules.setQualityRules(qualityRuleList);
        return qualityRules;
    }

    private QualityRule createQualityRule(String ruleId, String qgisExpression) {
        return createQualityRule(ruleId, "category1", "sql1", qgisExpression);
    }

    private QualityRule createQualityRuleWithCheckIfChanged(String sql, String... checkIfChanged) {
        QualityRule rule = new QualityRule();
        rule.setSql(sql);
        rule.setCheckIfChanged(Arrays.asList(checkIfChanged));
        return rule;
    }

    private QualityRule createQualityRule(String ruleId, String category, String sql, String qgisExpression) {
        QualityRule rule = new QualityRule();
        rule.setRuleId(ruleId);
        rule.setCategory(category);
        rule.setSql(sql);
        rule.setQgisExpression(qgisExpression);
        rule.setDescriptions(QualityCheckFixture.createDescriptions());
        return rule;
    }

    private Map<String, List<UUID>> createCriteria(String category, List<UUID> ids) {
        Map<String, List<UUID>> qualityCriteria = new HashMap<>();
        qualityCriteria.put(category, ids);
        return qualityCriteria;
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
        result.setErrorMessage("bad SQL grammar");
        return result;
    }
}
