package fi.nls.dbquality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import org.geolatte.geom.jts.JTS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import fi.nls.dbquality.RuleExecutorService;
import fi.nls.dbquality.model.BadQueryResult;
import fi.nls.dbquality.model.QualityQueryResult;
import net.postgis.jdbc.PGgeometry;

public class RuleExecutorTest {

    private static final String BASE_SQL = "SELECT * FROM schema.table";
    private static final String ID_FIELD = "id_field";
    private static final String TARGET_ID = "f63a52a7-c575-477f-896a-88fb16bb952a";
    private static final String RELATED_ID = "031afcc0-0b41-4612-978b-6b38593d5dbd";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ResultSet resultSet;

    @Captor
    private ArgumentCaptor<String> queryCaptor;

    private RuleExecutorService workRuleExecutorService;

    private UUID id1 = UUID.randomUUID();
    private UUID id2 = UUID.randomUUID();
    private List<UUID> ids;

    @BeforeEach
    void beforeEach() throws Exception {
        MockitoAnnotations.openMocks(this);
        workRuleExecutorService = new RuleExecutorService(ID_FIELD);
        ids = List.of(id1, id2);
        when(resultSet.getString(1)).thenReturn(TARGET_ID);
        when(resultSet.getObject(3)).thenReturn(RELATED_ID);
        when(resultSet.getString(3)).thenReturn(RELATED_ID);
    }

    @Test
    @DisplayName("Query errors are caught and BadSqlResult is returned")
    void queryErrorsAreCaught() {
        String errorMsg = "Error message";
        when(jdbcTemplate.query(anyString(), any(RuleExecutorService.QualityQueryResultRowMapper.class))).thenThrow(new RuntimeException(errorMsg));

        List<QualityQueryResult> results = workRuleExecutorService.executeRule(jdbcTemplate, BASE_SQL, ids);

        assertThat(results.size()).isEqualTo(2);
        assertThat(results).anySatisfy(result -> {
            assertThat(result.getTargetId()).isEqualTo(id1);
            assertThat(((BadQueryResult) result).getErrorMessage()).isEqualTo(errorMsg);
        });
        assertThat(results).anySatisfy(result -> {
            assertThat(result.getTargetId()).isEqualTo(id2);
            assertThat(((BadQueryResult) result).getErrorMessage()).isEqualTo(errorMsg);
        });
    }

    @Test
    @DisplayName("Single BadSqlResult is returned when no ids are given")
    void singleBadSqlResult() {
        String errorMsg = "Error message";
        when(jdbcTemplate.query(anyString(), any(RuleExecutorService.QualityQueryResultRowMapper.class))).thenThrow(new RuntimeException(errorMsg));

        List<QualityQueryResult> results = workRuleExecutorService.executeRule(jdbcTemplate, BASE_SQL, null);

        assertThat(results.size()).isEqualTo(1);
        BadQueryResult result1 = (BadQueryResult) results.get(0);
        assertThat(result1.getTargetId()).isNull();
        assertThat(result1.getErrorMessage()).isEqualTo(errorMsg);
    }

    @Test
    @DisplayName("Result row is mapped")
    void mapRow() throws Exception {
        PGgeometry geom = new PGgeometry("SRID=3067;POLYGON Z ((0 0 0, 1 0 0, 1 1 0, 0 1 0, 0 0 0))");
        when(resultSet.getObject(2)).thenReturn(geom);
        when(resultSet.getObject(2, PGgeometry.class)).thenReturn(geom);

        QualityQueryResult result = new RuleExecutorService.QualityQueryResultRowMapper().mapRow(resultSet, 0);

        assertThat(JTS.to(result.getGeometryError()).toText()).isEqualTo("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))");
    }

    @Test
    @DisplayName("Result row without srid is mapped")
    void mapRowWithoutSrid() throws Exception {
        PGgeometry geom = new PGgeometry("POINT (0 0 0)");
        when(resultSet.getObject(2)).thenReturn(geom);
        when(resultSet.getObject(2, PGgeometry.class)).thenReturn(geom);

        QualityQueryResult result = new RuleExecutorService.QualityQueryResultRowMapper().mapRow(resultSet, 0);

        assertThat(JTS.to(result.getGeometryError()).toText()).isEqualTo("POINT (0 0)");
    }

    @Test
    @DisplayName("Result row without geometry is mapped")
    void mapRowWithoutGeometry() throws Exception {
        when(resultSet.getObject(2)).thenReturn(null);

        QualityQueryResult result = new RuleExecutorService.QualityQueryResultRowMapper().mapRow(resultSet, 0);

        assertThat(result.getTargetId().toString()).isEqualTo(TARGET_ID);
        assertThat(result.getGeometryError()).isNull();
        assertThat(result.getRelatedId().toString()).isEqualTo(RELATED_ID);
    }

    @Test
    @DisplayName("Result row without related id is mapped")
    void mapRowWithoutRelatedId() throws Exception {
        when(resultSet.getObject(3)).thenReturn(null);

        QualityQueryResult result = new RuleExecutorService.QualityQueryResultRowMapper().mapRow(resultSet, 0);

        assertThat(result.getRelatedId()).isNull();
    }
}
