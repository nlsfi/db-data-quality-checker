package fi.nls.dbquality;

import org.geolatte.geom.C2D;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.codec.Wkt;

import net.postgis.jdbc.PGgeometry;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import fi.nls.dbquality.model.BadQueryResult;
import fi.nls.dbquality.model.QualityQueryResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RuleExecutorService {
    private static final int SRID_2D = 3067;
    @SuppressWarnings("unchecked")
    private static CoordinateReferenceSystem<C2D> referenceSystem = (CoordinateReferenceSystem<C2D>) CrsRegistry.getCoordinateReferenceSystemForEPSG(SRID_2D, null);;

    private String idField;

    public RuleExecutorService(String idField) {
        validateColumnName(idField);

        this.idField = idField;
    }

    private void validateColumnName(String columnName) {
        if (columnName.contains(";") || columnName.contains("--") || columnName.contains("/*")) {
            throw new IllegalArgumentException("invalid column name");
        }
    }

    public List<QualityQueryResult> executeRule(JdbcTemplate jdbcTemplate, String sql, List<UUID> ids) {
        String queryStr = QualityRuleSqlParser.parse(sql, ids, idField);
        boolean hasIdFilter = QualityRuleSqlParser.hasIdFilter(sql);

        try {
            var args = ids == null ? null : ids.toArray(new Object[0]);
            if (args != null && args.length > 0 && hasIdFilter) {
                return jdbcTemplate.query(queryStr, new QualityQueryResultRowMapper(), args);
            }
            return jdbcTemplate.query(queryStr, new QualityQueryResultRowMapper());
        } catch (RuntimeException e) {
            if (ids == null || ids.isEmpty()) {
                BadQueryResult result = new BadQueryResult();
                result.setErrorMessage(e.getMessage());
                return Collections.singletonList(result);
            }
            return ids.stream().map(id -> {
                BadQueryResult result = new BadQueryResult();
                result.setTargetId(id);
                result.setErrorMessage(e.getMessage());
                return result;
            }).collect(Collectors.toList());
        }
    }

    public static class QualityQueryResultRowMapper implements RowMapper<QualityQueryResult> {
        @Override
        public QualityQueryResult mapRow(ResultSet rs, int rowNum) throws SQLException {
            QualityQueryResult result = new QualityQueryResult();
            result.setTargetId(UUID.fromString(rs.getString(1)));
            if (rs.getObject(2) != null) {
                PGgeometry pgGeometry = rs.getObject(2, PGgeometry.class);
                String wkt = pgGeometry.getGeometry().toString();
                String[] splittedWkt = wkt.split(";");
                String geomStr;
                if (splittedWkt.length == 1) {
                    // "geometry"
                    geomStr = splittedWkt[0];
                } else if (splittedWkt.length == 2) {
                    // "srid;geometry"
                    geomStr = splittedWkt[1];
                } else {
                    throw new RuntimeException("Unexpected geometry string: " + wkt);
                }
                var geom = Wkt.fromWkt(geomStr, referenceSystem);
                result.setGeometryError(geom);
            }
            if (rs.getObject(3) != null) {
                result.setRelatedId(UUID.fromString(rs.getString(3)));
            }
            return result;
        }
    }
}
