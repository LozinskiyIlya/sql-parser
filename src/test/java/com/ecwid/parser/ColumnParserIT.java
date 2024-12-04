package com.ecwid.parser;

import com.ecwid.parser.fragment.enity.Aliasable;
import com.ecwid.parser.fragment.enity.Nameable;
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
            assertColumnEquals(parsed.getColumns().getFirst(), "*", null);
        }

        @Test
        @DisplayName("only one column")
        void oneColumn() throws Exception {
            final var sql = "SELECT a FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertColumnEquals(parsed.getColumns().getFirst(), "a", null);
        }

        @Test
        @DisplayName("only one column with alias")
        void oneColumnWithAlias() throws Exception {
            final var sql = "SELECT a as b FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertColumnEquals(parsed.getColumns().getFirst(), "a", "b");
        }

        @Test
        @DisplayName("multiple columns")
        void multipleColumns() throws Exception {
            final var sql = "SELECT a, b, c FROM table;";
            final var parsed = sqlParser.parse(sql);
            System.out.println(parsed.getColumns());
            assertEquals(3, parsed.getColumns().size());
            assertColumnEquals(parsed.getColumns().get(0), "a", null);
            assertColumnEquals(parsed.getColumns().get(1), "b", null);
            assertColumnEquals(parsed.getColumns().get(2), "c", null);
        }

        @Test
        @DisplayName("multiple columns with alias")
        void multipleColumnsWithAlias() throws Exception {
            final var sql = "SELECT a, b as 2, c FROM table;";
            final var parsed = sqlParser.parse(sql);
            System.out.println(parsed.getColumns());
            assertEquals(3, parsed.getColumns().size());
            assertColumnEquals(parsed.getColumns().get(0), "a", null);
            assertColumnEquals(parsed.getColumns().get(1), "b", "2");
            assertColumnEquals(parsed.getColumns().get(2), "c", null);
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
            System.out.println(parsed);
            assertEquals(2, parsed.getColumns().size());
            assertColumnEquals(parsed.getColumns().get(0), "count(*)", null);
            assertColumnEquals(parsed.getColumns().get(1), "a", null);
        }

        @Test
        @DisplayName("column name before function")
        void simpleNameAndFunction() throws Exception {
            final var sql = "SELECT a, count(*) FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getColumns().size());
            assertColumnEquals(parsed.getColumns().get(0), "a", null);
            assertColumnEquals(parsed.getColumns().get(1), "count(*)", null);
        }

        @Test
        @DisplayName("column name inside function")
        void functionWithColumnNameInside() throws Exception {
            final var sql = "SELECT min(cost) FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertColumnEquals(parsed.getColumns().getFirst(), "min(cost)", null);
        }

        @Test
        @DisplayName("multiple columns and functions")
        void simpleNameAndCount() throws Exception {
            final var sql = "SELECT a, max(cost) as m, avg(t) as a, b as d, c, count(*) as c1 FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(6, parsed.getColumns().size());
            assertColumnEquals(parsed.getColumns().get(0), "a", null);
            assertColumnEquals(parsed.getColumns().get(1), "max(cost)", "m");
            assertColumnEquals(parsed.getColumns().get(2), "avg(t)", "a");
            assertColumnEquals(parsed.getColumns().get(3), "b", "d");
            assertColumnEquals(parsed.getColumns().get(4), "c", null);
            assertColumnEquals(parsed.getColumns().get(5), "count(*)", "c1");
        }
    }

    private void assertColumnEquals(Nameable column, String name, String alias) {
        assertEquals(name, column.name());
        assertEquals(alias, column.alias());
    }
}
