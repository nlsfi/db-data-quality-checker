package fi.nls.dbquality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class QualityRuleSqlParserTest {

    private static final String ID_FIELD = "id_field_name";

    @Test
    void parseShouldThrowNullPointerExceptionIfSqlIsNull() {
        assertThrows(NullPointerException.class, () -> {
            QualityRuleSqlParser.parse(null, null, null);
        });
    }

    @Test
    void parseShouldThrowNullPointerExceptionIfIdsIsNotNullAndIdFieldIsNull() {
        assertThrows(NullPointerException.class, () -> {
            QualityRuleSqlParser.parse(null, List.of(UUID.randomUUID()), null);
        });
    }

    @Test
    void parseShouldReturnSameSqlIfNoSpecialKeyWords() {
        String sql = "select * from tablename where a = b;";
        String result = QualityRuleSqlParser.parse(sql, null, null);
        assertEquals(sql, result);
    }

    @Test
    void parseShouldReplaceSourceIdFilterWithTrueConditionIfIdListIsNull() {
        String sql = "select * from tablename where :source_id_filter;";
        String expectedSql = "select * from tablename where 1=1;";
        String result = QualityRuleSqlParser.parse(sql, null, null);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceMultipleSourceIdFiltersWithTrueConditionIfIdListIsNull() {
        String sql = "select * from tablename where :source_id_filter and :source_id_filter or :source_id_filter;";
        String expectedSql = "select * from tablename where 1=1 and 1=1 or 1=1;";
        String result = QualityRuleSqlParser.parse(sql, null, null);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceSourceIdFilterWithFalseConditionIfIdListIsEmpty() {
        String sql = "select * from tablename where :source_id_filter;";
        String expectedSql = "select * from tablename where 1=0;";
        String result = QualityRuleSqlParser.parse(sql, Collections.emptyList(), ID_FIELD);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceMultipleSourceIdFiltersWithFalseConditionIfIdListIsEmpty() {
        String sql = "select * from tablename where :source_id_filter and :source_id_filter or :source_id_filter;";
        String expectedSql = "select * from tablename where 1=0 and 1=0 or 1=0;";
        String result = QualityRuleSqlParser.parse(sql, Collections.emptyList(), ID_FIELD);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceSourceIdFilterWithInClauseWhenOneIdInList() {
        UUID id1 = UUID.fromString("b6c23946-3ed7-4b83-9568-e82767e39287");
        String sql = "select * from tablename where :source_id_filter;";

        String expectedSql = "select * from tablename where s." + ID_FIELD + " IN (?);";
        String result = QualityRuleSqlParser.parse(sql, List.of(id1), ID_FIELD);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceMultipleSourceIdFiltersWithInClauseWhenOneIdInList() {
        UUID id1 = UUID.fromString("b6c23946-3ed7-4b83-9568-e82767e39287");
        String sql = "select * from tablename where :source_id_filter and :source_id_filter;";

        String expectedSql = "select * from tablename where s." + ID_FIELD + " IN (?) and s." + ID_FIELD + " IN (?);";
        String result = QualityRuleSqlParser.parse(sql, List.of(id1), ID_FIELD);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceSourceIdFilterWithInClauseWhenMultipleIdsInList() {
        UUID id1 = UUID.fromString("b6c23946-3ed7-4b83-9568-e82767e39287");
        UUID id2 = UUID.fromString("25895a30-2a97-4d8f-8a5b-311a368a352a");
        UUID id3 = UUID.fromString("f50a5c61-4826-42d9-81fd-098d9546df07");
        String sql = "select * from tablename where :source_id_filter;";
        String expectedSql = "select * from tablename where s." + ID_FIELD + " IN (?,?,?);";
        String result = QualityRuleSqlParser.parse(sql, List.of(id1, id2, id3), ID_FIELD);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceMultipleSourceIdFiltersWithInClauseWhenMultipleIdsInList() {
        UUID id1 = UUID.fromString("b6c23946-3ed7-4b83-9568-e82767e39287");
        UUID id2 = UUID.fromString("25895a30-2a97-4d8f-8a5b-311a368a352a");
        UUID id3 = UUID.fromString("f50a5c61-4826-42d9-81fd-098d9546df07");
        String sql = "select * from tablename where :source_id_filter and :source_id_filter;";

        String expectedSql = "select * from tablename where s." + ID_FIELD + " IN (?,?,?) and s."
                + ID_FIELD + " IN (?,?,?);";
        String result = QualityRuleSqlParser.parse(sql, List.of(id1, id2, id3), ID_FIELD);
        assertEquals(expectedSql, result);
    }
}
