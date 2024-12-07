package com.ecwid.parser;

import com.ecwid.parser.fragment.Column;
import com.ecwid.parser.fragment.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("When columns include")
public class ColumnParserIT extends AbstractSpringParserTest {

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
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().get(0));
            assertFragmentEquals(Column.class, "b", null, parsed.getColumns().get(1));
            assertFragmentEquals(Column.class, "c", null, parsed.getColumns().get(2));
        }

        @Test
        @DisplayName("multiple columns with alias")
        void multipleColumnsWithAlias() throws Exception {
            final var sql = "SELECT a, b as 2, c FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().get(0));
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
            assertFragmentEquals(Column.class, "count(*)", null, parsed.getColumns().get(0));
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().get(1));
        }

        @Test
        @DisplayName("column name before function")
        void simpleNameAndFunction() throws Exception {
            final var sql = "SELECT a, count(*) FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getColumns().size());
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().get(0));
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
            assertFragmentEquals(Column.class, "a", null, parsed.getColumns().get(0));
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
        @DisplayName("one own column")
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
        }

        @Test
        @DisplayName("multiple own columns")
        void nestedQueryWithMultipleColumns() throws Exception {

        }

        @Test
        @DisplayName("multiple own columns their alias and own alias")
        void nestedQueryWithMultipleColumnsAndAlias() throws Exception {

        }

        @Test
        @DisplayName("several nested queries and column names")
        void severalNestedQueries() throws Exception {

        }
    }
}
