package fi.nls.dbquality;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class QualityRuleSqlParser {

    private static final String SOURCE_ID_PREFIX = "s.";
    private static final String SOURCE_ID_FILTER = ":source_id_filter";
    private static final String SQL_TRUE = "1=1";
    private static final String SQL_FALSE = "1=0";

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
            sb.append(String.join(", ", ids.stream().map(id -> "'" + id + "'").collect(Collectors.toList())));
            sb.append(")");
            return sql.replace(SOURCE_ID_FILTER, sb.toString());
        }
    }
}
