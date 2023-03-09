package fi.nls.dbquality;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class QualityRuleSqlParser {

    private static final String SOURCE_ID_PREFIX = "s.";
    private static final String SOURCE_ID_FILTER = ":source_id_filter";
    private static final String SQL_TRUE = "1=1";
    private static final String SQL_FALSE = "1=0";

    private static String getPlaceholders(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        if (count > 1) {
            sb.append(",?".repeat(count - 1));
        }
        return sb.toString();
    }

    public static String parse(String sql, List<UUID> ids, String idField) {
        Objects.requireNonNull(sql, "sql must not be null");
        if (ids != null) {
            Objects.requireNonNull(idField, "idField must not be null if ids is not null");
        }
        if (ids == null) {
            // if id list is null -> replace source_id_filter with 1=1
            return sql.replace(SOURCE_ID_FILTER, SQL_TRUE);
        } else if (ids.isEmpty()) {
            // if id list is empty -> replace source_id_filter with 1=0
            return sql.replace(SOURCE_ID_FILTER, SQL_FALSE);
        } else {
            // if there are ids in list -> replace source_id_filter with in clause
            StringBuilder sb = new StringBuilder();
            sb.append(SOURCE_ID_PREFIX).append(idField).append(" IN (");
            sb.append(QualityRuleSqlParser.getPlaceholders(ids.size()));
            sb.append(")");
            return sql.replace(SOURCE_ID_FILTER, sb.toString());
        }
    }
}
