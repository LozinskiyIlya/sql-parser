package com.ecwid.parser;

import com.ecwid.parser.fragment.Column;
import com.ecwid.parser.fragment.Constant;
import com.ecwid.parser.fragment.Query;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("When columns include")
public class ColumnParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("no source")
    class NoSource {
        @Test
        @DisplayName("no from")
        void noFrom() throws Exception {
            final var sql = "SELECT 1";
            final var parsed = sqlParser.parse(sql);
            assertEqualsIgnoreCaseTrimmed(sql, parsed.getValue());
        }

        @Test
        @DisplayName("no from with alias")
        void noFromWithAlias() throws Exception {
            final var sql = "SELECT '1' b, 2 as C;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getColumns().size());
            assertFragmentEquals(Constant.class, "'1'", "b", parsed.getColumns().getFirst());
            assertFragmentEquals(Constant.class, "2", "c", parsed.getColumns().get(1));
        }

        @Test
        @DisplayName("with nested queries")
        void nestedQuery() throws Exception {
            final var nested = "SELECT a b, c d FROM table";
            final var sql = "SELECT (%s) T, 1, (%s), 2".formatted(nested, nested);
            final var parsed = sqlParser.parse(sql);
            assertEqualsIgnoreCaseTrimmed(sql, parsed.getValue());
        }

        @Test
        @DisplayName("with other clauses")
        void withOtherClauses() throws Exception {
            final var sql = "SELECT 1, 2, 3 WHERE a = 1 LIMIT 1 OFFSET 2";
            final var parsed = sqlParser.parse(sql);
            assertEqualsIgnoreCaseTrimmed(sql, parsed.getValue());
        }
    }

    @Nested
    @DisplayName("column names with")
    class ColumnNames {

        @Test
        @DisplayName("only asterisk")
        void onlyAsterisk() throws Exception {
            final var sql = "SELECT * FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "*", null, parsed.getColumns().getFirst());
        }

        @Test
        @DisplayName("only one column")
        void oneColumn() throws Exception {
            final var sql = "SELECT a FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().getFirst());
        }

        @Test
        @DisplayName("only one column with alias")
        void oneColumnWithAlias() throws Exception {
            final var sql = "SELECT a as b FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "a", "b", parsed.getColumns().getFirst());
        }

        @Test
        @DisplayName("multiple columns")
        void multipleColumns() throws Exception {
            final var sql = "SELECT a, b, c FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().getFirst());
            assertFragmentEquals(Column.class, "b", null, parsed.getColumns().get(1));
            assertFragmentEquals(Column.class, "c", null, parsed.getColumns().get(2));
        }

        @Test
        @DisplayName("multiple columns with alias")
        void multipleColumnsWithAlias() throws Exception {
            final var sql = "SELECT a, b as 2, c FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().getFirst());
            assertFragmentEquals(Column.class, "b", "2", parsed.getColumns().get(1));
            assertFragmentEquals(Column.class, "c", null, parsed.getColumns().get(2));
        }
    }

    @Nested
    @DisplayName("functions with")
    class Functions {

        @Test
        @DisplayName("column name after function")
        void functionAndSimpleName() throws Exception {
            final var sql = "SELECT count(*), a FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "count(*)", null, parsed.getColumns().getFirst());
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().get(1));
        }

        @Test
        @DisplayName("column name before function")
        void simpleNameAndFunction() throws Exception {
            final var sql = "SELECT a, count(*) FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().getFirst());
            assertFragmentEquals(Column.class, "count(*)", null, parsed.getColumns().get(1));
        }

        @Test
        @DisplayName("column name inside function")
        void functionWithColumnNameInside() throws Exception {
            final var sql = "SELECT min(cost) FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "min(cost)", null, parsed.getColumns().getFirst());
        }

        @Test
        @DisplayName("multiple columns and functions")
        void simpleNameAndCount() throws Exception {
            final var sql = "SELECT a, max(cost) as m, avg(t) as a, b as d, c, count(*) as c1 FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(6, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().getFirst());
            assertFragmentEquals(Column.class, "max(cost)", "m", parsed.getColumns().get(1));
            assertFragmentEquals(Column.class, "avg(t)", "a", parsed.getColumns().get(2));
            assertFragmentEquals(Column.class, "b", "d", parsed.getColumns().get(3));
            assertFragmentEquals(Column.class, "c", null, parsed.getColumns().get(4));
            assertFragmentEquals(Column.class, "count(*)", "c1", parsed.getColumns().get(5));
        }
    }

    @Nested
    @DisplayName("nested query with")
    class NestedQuery {

        @Test
        @DisplayName("one nested column")
        void nestedQuery() throws Exception {
            final var nested = "SELECT a FROM table";
            final var sql = "SELECT (%s) FROM table;".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Query.class, nested, null, parsed.getColumns().getFirst());
        }

        @Test
        @DisplayName("alias")
        void nestedQueryWithAlias() throws Exception {
            final var nested = "SELECT a FROM table";
            final var sql = "SELECT (%s) T FROM table;".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Query.class, nested, "t", parsed.getColumns().getFirst());
        }

        @Test
        @DisplayName("alias with AS")
        void nestedQueryWithAsAlias() throws Exception {
            final var nested = "SELECT a FROM table";
            final var sql = "SELECT (%s) AS T FROM table;".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Query.class, nested, "t", parsed.getColumns().getFirst());
            final var nestedQuery = (Query) parsed.getColumns().getFirst();
            assertEquals(1, nestedQuery.getColumns().size());
            assertFragmentEquals(Column.class, "a", null, nestedQuery.getColumns().getFirst());
        }

        @Test
        @DisplayName("multiple nested columns")
        void nestedQueryWithMultipleNestedColumns() throws Exception {
            final var nested = "SELECT a, b FROM table";
            final var sql = "SELECT (%s) FROM table;".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Query.class, nested, null, parsed.getColumns().getFirst());
            final var nestedQuery = (Query) parsed.getColumns().getFirst();
            assertEquals(2, nestedQuery.getColumns().size());
            assertFragmentEquals(Column.class, "a", null, nestedQuery.getColumns().getFirst());
            assertFragmentEquals(Column.class, "b", null, nestedQuery.getColumns().get(1));
        }

        @Test
        @DisplayName("multiple nested columns their alias and own alias")
        void nestedQueryWithMultipleColumnsAndAlias() throws Exception {
            final var nested = "SELECT a b, c d FROM table";
            final var sql = "SELECT (%s) AS T FROM table;".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Query.class, nested, "t", parsed.getColumns().getFirst());
            final var nestedQuery = (Query) parsed.getColumns().getFirst();
            assertEquals(2, nestedQuery.getColumns().size());
            assertFragmentEquals(Column.class, "a", "b", nestedQuery.getColumns().getFirst());
            assertFragmentEquals(Column.class, "c", "d", nestedQuery.getColumns().get(1));
        }

        @Test
        @DisplayName("several nested queries and column names")
        void severalNestedQueries() throws Exception {
            final var nested1 = "SELECT a b, c d FROM table";
            final var nested2 = "SELECT e, f FROM table";
            final var sql = "SELECT x, (%s) AS T, (%s), y FROM table;".formatted(nested1, nested2);
            final var parsed = sqlParser.parse(sql);
            assertEquals(4, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "x", null, parsed.getColumns().getFirst());
            assertFragmentEquals(Query.class, nested1, "t", parsed.getColumns().get(1));

            final var nestedQuery1 = (Query) parsed.getColumns().get(1);
            assertEquals(2, nestedQuery1.getColumns().size());
            assertFragmentEquals(Column.class, "a", "b", nestedQuery1.getColumns().getFirst());
            assertFragmentEquals(Column.class, "c", "d", nestedQuery1.getColumns().get(1));

            assertFragmentEquals(Query.class, nested2, null, parsed.getColumns().get(2));

            final var nestedQuery2 = (Query) parsed.getColumns().get(2);
            assertEquals(2, nestedQuery2.getColumns().size());
            assertFragmentEquals(Column.class, "e", null, nestedQuery2.getColumns().getFirst());
            assertFragmentEquals(Column.class, "f", null, nestedQuery2.getColumns().get(1));

            assertFragmentEquals(Column.class, "y", null, parsed.getColumns().get(3));
        }

        @Test
        @DisplayName("2 levels of nested queries")
        void twoLevelsOfNestedQueries() throws Exception {
            final var nested1 = "SELECT a b, c d FROM table";
            final var nested2 = "SELECT e, f FROM table";
            final var sql = "SELECT x, (%s) AS T, (%s), y FROM table;".formatted(nested1, nested2);
            final var parsed = sqlParser.parse(sql);
            assertEquals(4, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "x", null, parsed.getColumns().getFirst());
            assertFragmentEquals(Query.class, nested1, "t", parsed.getColumns().get(1));
            assertFragmentEquals(Query.class, nested2, null, parsed.getColumns().get(2));
            assertFragmentEquals(Column.class, "y", null, parsed.getColumns().get(3));
            final var nestedQuery1 = (Query) parsed.getColumns().get(1);
            assertEquals(2, nestedQuery1.getColumns().size());
            assertFragmentEquals(Column.class, "a", "b", nestedQuery1.getColumns().getFirst());
            assertFragmentEquals(Column.class, "c", "d", nestedQuery1.getColumns().get(1));
            final var nestedQuery2 = (Query) parsed.getColumns().get(2);
            assertEquals(2, nestedQuery2.getColumns().size());
            assertFragmentEquals(Column.class, "e", null, nestedQuery2.getColumns().getFirst());
            assertFragmentEquals(Column.class, "f", null, nestedQuery2.getColumns().get(1));
        }
    }

    @Nested
    @DisplayName("constant with")
    class ConstantCol {

        @Test
        @DisplayName("alias")
        void constantWithAlias() throws Exception {
            final var sql = "SELECT 1 a FROM table";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Constant.class, "1", "a", parsed.getColumns().getFirst());
        }

        @Test
        @DisplayName("as alias")
        void constantAsAlias() throws Exception {
            final var sql = "SELECT 1 AS a FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertFragmentEquals(Constant.class, "1", "a", parsed.getColumns().getFirst());
        }

        @Test
        @DisplayName("other columns")
        void constantWithOtherColumns() throws Exception {
            final var sql = "SELECT 1, a, 2 as b, c FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(4, parsed.getColumns().size());
            assertFragmentEquals(Constant.class, "1", null, parsed.getColumns().getFirst());
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().get(1));
            assertFragmentEquals(Constant.class, "2", "b", parsed.getColumns().get(2));
            assertFragmentEquals(Column.class, "c", null, parsed.getColumns().get(3));
        }


        @TestFactory
        @DisplayName("various constant types")
        Stream<DynamicTest> variousConstants() {
            return Stream.of(1, 1.0, -1, -1.0, "'a'", "'a;();.,b'")
                    .map(it -> DynamicTest.dynamicTest("type:" + it, () -> {
                        final var sql = "SELECT %s AS t FROM table;".formatted(it);
                        final var parsed = sqlParser.parse(sql);
                        assertEquals(1, parsed.getColumns().size());
                        assertFragmentEquals(Constant.class, String.valueOf(it), "t", parsed.getColumns().getFirst());
                    }));
        }
    }

    @Test
    @DisplayName("all that beauty")
    void withAllThatBeauty() throws IOException {
        final var nested1 = "SELECT a b, '1' c, 2 d, count(a.id) e FROM table";
        final var nested2 = "SELECT f g, 2, max(h), (%s), max(g) FROM table".formatted(nested1);
        final var sql = "SELECT x, (%s), (%s) 'i', y z FROM table;".formatted(nested1, nested2);
        final var parsed = sqlParser.parse(sql);
        assertEquals(4, parsed.getColumns().size());
        assertFragmentEquals(Column.class, "x", null, parsed.getColumns().getFirst());
        assertFragmentEquals(Query.class, nested1, null, parsed.getColumns().get(1));
        assertFragmentEquals(Query.class, nested2, "'i'", parsed.getColumns().get(2));
        assertFragmentEquals(Column.class, "y", "z", parsed.getColumns().get(3));
        final var nestedQuery1 = (Query) parsed.getColumns().get(1);
        assertEquals(4, nestedQuery1.getColumns().size());
        assertFragmentEquals(Column.class, "a", "b", nestedQuery1.getColumns().getFirst());
        assertFragmentEquals(Constant.class, "'1'", "c", nestedQuery1.getColumns().get(1));
        assertFragmentEquals(Constant.class, "2", "d", nestedQuery1.getColumns().get(2));
        assertFragmentEquals(Column.class, "count(a.id)", "e", nestedQuery1.getColumns().get(3));
        final var nestedQuery2 = (Query) parsed.getColumns().get(2);
        assertEquals(5, nestedQuery2.getColumns().size());
        assertFragmentEquals(Column.class, "f", "g", nestedQuery2.getColumns().getFirst());
        assertFragmentEquals(Constant.class, "2", null, nestedQuery2.getColumns().get(1));
        assertFragmentEquals(Column.class, "max(h)", null, nestedQuery2.getColumns().get(2));
        assertFragmentEquals(Query.class, nested1, null, nestedQuery2.getColumns().get(3));
        assertFragmentEquals(Column.class, "max(g)", null, nestedQuery2.getColumns().get(4));
    }
}
