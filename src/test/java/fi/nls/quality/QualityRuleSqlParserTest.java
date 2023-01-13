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
        String expectedSql = "select * from tablename where s." + ID_FIELD + " IN ('" + id1 + "');";
        String result = QualityRuleSqlParser.parse(sql, List.of(id1), ID_FIELD);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceMultipleSourceIdFiltersWithInClauseWhenOneIdInList() {
        UUID id1 = UUID.fromString("b6c23946-3ed7-4b83-9568-e82767e39287");
        String sql = "select * from tablename where :source_id_filter and :source_id_filter;";
        String expectedSql = "select * from tablename where s." + ID_FIELD + " IN ('" + id1 + "') and s." + ID_FIELD + " IN ('" + id1
                + "');";
        String result = QualityRuleSqlParser.parse(sql, List.of(id1), ID_FIELD);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceSourceIdFilterWithInClauseWhenMultipleIdsInList() {
        UUID id1 = UUID.fromString("b6c23946-3ed7-4b83-9568-e82767e39287");
        UUID id2 = UUID.fromString("25895a30-2a97-4d8f-8a5b-311a368a352a");
        UUID id3 = UUID.fromString("f50a5c61-4826-42d9-81fd-098d9546df07");
        String sql = "select * from tablename where :source_id_filter;";
        String expectedSql = "select * from tablename where s." + ID_FIELD + " IN ('" + id1 + "', '" + id2 + "', '" + id3 + "');";
        String result = QualityRuleSqlParser.parse(sql, List.of(id1, id2, id3), ID_FIELD);
        assertEquals(expectedSql, result);
    }

    @Test
    void parseShouldReplaceMultipleSourceIdFiltersWithInClauseWhenMultipleIdsInList() {
        UUID id1 = UUID.fromString("b6c23946-3ed7-4b83-9568-e82767e39287");
        UUID id2 = UUID.fromString("25895a30-2a97-4d8f-8a5b-311a368a352a");
        UUID id3 = UUID.fromString("f50a5c61-4826-42d9-81fd-098d9546df07");
        String sql = "select * from tablename where :source_id_filter and :source_id_filter;";
        String expectedSql = "select * from tablename where s." + ID_FIELD + " IN ('" + id1 + "', '" + id2 + "', '" + id3 + "') and s."
                + ID_FIELD + " IN ('" + id1 + "', '" + id2 + "', '" + id3 + "');";
        String result = QualityRuleSqlParser.parse(sql, List.of(id1, id2, id3), ID_FIELD);
        assertEquals(expectedSql, result);
    }

}
