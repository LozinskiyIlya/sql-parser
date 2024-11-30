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
    @DisplayName("count and simple column name")
    void countAndSimpleName() throws Exception {
        final var sql = "SELECT count(*), a FROM table;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(2, parsed.getColumns().size());
        assertEquals("count(*)", parsed.getColumns().get(0));
        assertEquals("a", parsed.getColumns().get(1));
    }
}
