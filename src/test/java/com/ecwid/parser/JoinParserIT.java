package com.ecwid.parser;

import com.ecwid.parser.fragment.Join;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("When join includes")
public class JoinParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("one JOIN with")
    class OneJoin {

        @Test
        @DisplayName("just JOIN")
        void join() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON table1.id = table2.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(Join.JoinType.JOIN, "table2", null, "table1.id", "table2.id", join);
        }

        @Test
        @DisplayName("alias")
        void joinWithAlias() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 t ON table1.id = t.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(Join.JoinType.JOIN, "table2", "t", "table1.id", "t.id", join);
        }

        @Test
        @DisplayName("as alias")
        void joinWithAsAlias() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 as t ON table1.id = t.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(Join.JoinType.JOIN, "table2", "t", "table1.id", "t.id", join);
        }

        @Test
        @DisplayName("multiple columns")
        void joinWithMultipleColumns() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON table1.id1 = table2.id1 AND table1.id2 = table2.id2;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(Join.JoinType.JOIN, "table2", null, "table1.id1", "table2.id1", join);
        }

        @Test
        @DisplayName("complex join type")
        void joinWithComplexType() throws Exception {
            final var sql = "SELECT * FROM table1 NATURAL FULL OUTER JOIN table2 ON table1.id = table2.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(Join.JoinType.NATURAL_FULL_OUTER, "table2", null, "table1.id", "table2.id", join);
        }
    }

    @TestFactory
    @DisplayName("various join types")
    Stream<DynamicTest> variousJoinTypes() {
        return Arrays.stream(Join.JoinType.values()).map(joinType -> {
            final var sql = "SELECT * FROM table " + joinType.getFullLexeme() + " table1 ON a = b;";
            return DynamicTest.dynamicTest(joinType.name(), () -> {
                System.out.println(sql);
                final var parsed = sqlParser.parse(sql);
                assertEquals(1, parsed.getJoins().size());
                final var join = parsed.getJoins().getFirst();
                assertJoinEquals(joinType, "table1", null, "a", "b", join);
            });
        });
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
