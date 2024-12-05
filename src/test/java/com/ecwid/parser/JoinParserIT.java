package com.ecwid.parser;

import com.ecwid.parser.fragment.Join;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("When join includes")
public class JoinParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("basic")
    class Basic {

        @Test
        @DisplayName("JOIN")
        void join() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON table1.id = table2.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(Join.JoinType.JOIN, "table2", null, "table1.id", "table2.id", join);
        }

        @Test
        @DisplayName("JOIN with alias")
        void joinWithAlias() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 t ON table1.id = t.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(Join.JoinType.JOIN, "table2", "t", "table1.id", "t.id", join);
        }

        @Test
        @DisplayName("JOIN with as alias")
        void joinWithAsAlias() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 as t ON table1.id = t.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(Join.JoinType.JOIN, "table2", "t", "table1.id", "t.id", join);
        }
    }

    private void assertJoinEquals(Join.JoinType type, String tableName, String tableAlias, String leftColumn, String rightColumn, Join join) {
        assertEquals(type, join.getType());
        final var table = join.getTable();
        assertEquals(tableName, table.name());
        assertEquals(tableAlias, table.alias());
        assertEquals(leftColumn, join.getLeftColumn().name());
        assertEquals(rightColumn, join.getRightColumn().name());
    }
}
