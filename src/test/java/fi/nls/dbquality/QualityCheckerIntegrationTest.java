package fi.nls.dbquality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import fi.nls.dbquality.model.BadQueryQualityResult;
import fi.nls.dbquality.model.QualityResult;
import fi.nls.dbquality.model.QualityRule;

public class QualityCheckerIntegrationTest {

    private static final String ID_FIELD = "id";
    private static final String SQL = "SELECT " + ID_FIELD + ", geom, related_id FROM test_table s WHERE :source_id_filter";
    private QualityService qualityService;
    private JdbcDataSource dataSource;
    private UUID uuid1;
    private UUID uuid2;

    @BeforeEach
    void beforeEach() {
        qualityService = new QualityService(ID_FIELD);
        uuid1 = UUID.randomUUID();
        uuid2 = UUID.randomUUID();
        dataSource = new JdbcDataSource();
        // keep db alive until vm is terminated
        dataSource.setURL("jdbc:h2:mem:test_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        initTestDb(dataSource);
    }

    @Test
    void executeRulesWithoutBindVariables() {
        QualityRule rule = QualityRuleFixture.createQualityRule("rule1", SQL);
        var qualityRunResult = qualityService.executeRules(dataSource, null, List.of(rule));

        var results = qualityRunResult.getQualityResults();
        assertEquals(3, results.size());
        // verify that query was executed successfully
        assertFalse(results.get(0) instanceof BadQueryQualityResult);
    }

    @Test
    void executeRulesWithOneSourceIdFilter() {
        QualityRule rule = QualityRuleFixture.createQualityRule("rule1", SQL);
        var qualityRunResult = qualityService.executeRules(dataSource, List.of(uuid1, uuid2), List.of(rule));

        var results = qualityRunResult.getQualityResults();
        assertEquals(2, results.size());
        List<UUID> uuids = results.stream().map(QualityResult::getTargetId).collect(Collectors.toList());
        assertTrue(uuids.contains(uuid1));
        assertTrue(uuids.contains(uuid2));
        // verify that query was executed successfully
        assertFalse(results.get(0) instanceof BadQueryQualityResult);
    }

    @Test
    void executeRulesWithSeveralSourceIdFilters() {
        String sql = SQL + " AND :source_id_filter";
        QualityRule rule = QualityRuleFixture.createQualityRule("rule1", sql);
        var qualityRunResult = qualityService.executeRules(dataSource, List.of(uuid1, uuid2), List.of(rule));

        var results = qualityRunResult.getQualityResults();
        assertEquals(2, results.size());
        List<UUID> uuids = results.stream().map(QualityResult::getTargetId).collect(Collectors.toList());
        assertTrue(uuids.contains(uuid1));
        assertTrue(uuids.contains(uuid2));
        // verify that query was executed successfully
        assertFalse(results.get(0) instanceof BadQueryQualityResult);
    }

    private void initTestDb(DataSource dataSource) {
        var jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("DROP TABLE IF EXISTS test_table");
        String createSql = String.format("CREATE TABLE test_table (%s uuid, geom text, related_id uuid)", ID_FIELD);
        jdbcTemplate.execute(createSql);
        insertTestRow(jdbcTemplate, uuid1);
        insertTestRow(jdbcTemplate, uuid2);
        insertTestRow(jdbcTemplate, UUID.randomUUID());
    }

    private void insertTestRow(JdbcTemplate jdbcTemplate, UUID uuid) {
        String insertSql = String.format("INSERT INTO test_table SET %s = '%s', geom = NULL, related_id = NULL", ID_FIELD, uuid);
        jdbcTemplate.execute(insertSql);
    }
}
