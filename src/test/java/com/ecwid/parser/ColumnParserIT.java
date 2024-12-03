package com.ecwid.parser;

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
            assertEquals("*", parsed.getColumns().getFirst().name());
        }

        @Test
        @DisplayName("only one column")
        void oneColumn() throws Exception {
            final var sql = "SELECT a FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertEquals("a", parsed.getColumns().getFirst().name());
        }

        @Test
        @DisplayName("multiple columns")
        void multipleColumns() throws Exception {
            final var sql = "SELECT a, b, c FROM table;";
            final var parsed = sqlParser.parse(sql);
            System.out.println(parsed.getColumns());
            assertEquals(3, parsed.getColumns().size());
            assertEquals("a", parsed.getColumns().get(0).name());
            assertEquals("b", parsed.getColumns().get(1).name());
            assertEquals("c", parsed.getColumns().get(2).name());
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
            assertEquals("count(*)", parsed.getColumns().get(0).name());
            assertEquals("a", parsed.getColumns().get(1).name());
        }

        @Test
        @DisplayName("column name before function")
        void simpleNameAndFunction() throws Exception {
            final var sql = "SELECT a, count(*) FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getColumns().size());
            assertEquals("a", parsed.getColumns().get(0).name());
            assertEquals("count(*)", parsed.getColumns().get(1).name());
        }

        @Test
        @DisplayName("column name inside function")
        void functionWithColumnNameInside() throws Exception {
            final var sql = "SELECT min(cost) FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertEquals("min(cost)", parsed.getColumns().getFirst().name());
        }

        @Test
        @DisplayName("multiple columns and functions")
        void simpleNameAndCount() throws Exception {
            final var sql = "SELECT a, max(cost), avg(t), b, c, count(*) FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(6, parsed.getColumns().size());
            assertEquals("a", parsed.getColumns().get(0).name());
            assertEquals("max(cost)", parsed.getColumns().get(1).name());
            assertEquals("avg(t)", parsed.getColumns().get(2).name());
            assertEquals("b", parsed.getColumns().get(3).name());
            assertEquals("c", parsed.getColumns().get(4).name());
            assertEquals("count(*)", parsed.getColumns().get(5).name());
        }
    }
}
