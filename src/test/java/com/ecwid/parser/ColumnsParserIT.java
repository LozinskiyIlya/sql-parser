package com.ecwid.parser;

import com.ecwid.parser.fragment.source.TableSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("When columns include")
public class ColumnsParserIT extends AbstractSpringParserTest {

    @Test
    @DisplayName("only asterisk")
    void onlyAsterisk() throws Exception {
        final var sql = "SELECT * FROM table;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(1, parsed.getColumns().size());
        assertEquals("*", parsed.getColumns().getFirst());
        assertEquals(1, parsed.getFromSources().size());
        final var source = parsed.getFromSources().getFirst();
        assertEquals(TableSource.class, source.getClass());
        assertEquals("table", ((TableSource) source).getTableName());
    }

    @Test
    @DisplayName("multiple columns")
    void multipleColumns() throws Exception {
        final var sql = "SELECT a, b, c FROM table;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(3, parsed.getColumns().size());
        assertEquals("a", parsed.getColumns().get(0));
        assertEquals("b", parsed.getColumns().get(1));
        assertEquals("c", parsed.getColumns().get(2));
    }

    @Test
    @DisplayName("function and column name")
    void functionAndSimpleName() throws Exception {
        final var sql = "SELECT count(*), a FROM table;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(2, parsed.getColumns().size());
        assertEquals("count(*)", parsed.getColumns().get(0));
        assertEquals("a", parsed.getColumns().get(1));
    }

    @Test
    @DisplayName("column name and function")
    void simpleNameAndFunction() throws Exception {
        final var sql = "SELECT a, count(*) FROM table;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(2, parsed.getColumns().size());
        assertEquals("a", parsed.getColumns().get(0));
        assertEquals("count(*)", parsed.getColumns().get(1));
    }

    @Test
    @DisplayName("function with column name inside")
    void functionWithColumnNameInside() throws Exception {
        final var sql = "SELECT min(cost) FROM table;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(1, parsed.getColumns().size());
        assertEquals("min(cost)", parsed.getColumns().get(0));
    }

    @Test
    @DisplayName("columns and functions")
    void simpleNameAndCount() throws Exception {
        final var sql = "SELECT a, max(cost), avg(t), b, c, count(*) FROM table;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(6, parsed.getColumns().size());
        assertEquals("a", parsed.getColumns().get(0));
        assertEquals("max(cost)", parsed.getColumns().get(1));
        assertEquals("avg(t)", parsed.getColumns().get(2));
        assertEquals("b", parsed.getColumns().get(3));
        assertEquals("c", parsed.getColumns().get(4));
        assertEquals("count(*)", parsed.getColumns().get(5));
    }
}
