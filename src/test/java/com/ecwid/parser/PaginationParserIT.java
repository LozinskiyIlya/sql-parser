package com.ecwid.parser;

import com.ecwid.parser.fragment.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("When pagination includes")
public class PaginationParserIT extends AbstractSpringParserTest {

    @Test
    @DisplayName("limit")
    void limit() throws Exception {
        final var sql = "SELECT * FROM table LIMIT 10;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(10, parsed.getLimit());
    }

    @Test
    @DisplayName("offset")
    void offset() throws Exception {
        final var sql = "SELECT * FROM table OFFSET 5;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(5, parsed.getOffset());
    }

    @Test
    @DisplayName("offset with rows")
    void offsetWithRows() throws Exception {
        final var sql = "SELECT * FROM table OFFSET 5 ROWS";
        final var parsed = sqlParser.parse(sql);
        assertEquals(5, parsed.getOffset());
    }

    @Test
    @DisplayName("limit and offset")
    void limitAndOffset() throws Exception {
        final var sql = "SELECT * FROM table LIMIT 10 OFFSET 5;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(10, parsed.getLimit());
        assertEquals(5, parsed.getOffset());
    }

    @Test
    @DisplayName("offset and limit")
    void offsetAndLimit() throws Exception {
        final var sql = "SELECT * FROM table OFFSET 5 LIMIT 20;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(20, parsed.getLimit());
        assertEquals(5, parsed.getOffset());
    }

    @Test
    @DisplayName("offset with rows and limit")
    void offsetWithRowsAndLimit() throws Exception {
        final var sql = "SELECT * FROM table OFFSET 5 ROWS LIMIT 20;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(20, parsed.getLimit());
        assertEquals(5, parsed.getOffset());
    }

    @Test
    @DisplayName("offset in inner limit in outer")
    void offsetInNestedQuery() throws IOException {
        final var sql = "SELECT * FROM (SELECT * FROM table OFFSET 5 ROWS) AS t LIMIT 1;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(1, parsed.getLimit());
        final var nested = (Query) parsed.getSources().getFirst();
        assertEquals(5, nested.getOffset());
    }

    @Test
    @DisplayName("with all that beauty")
    void withAllThatBeauty() throws IOException {
        final var sql = "SELECT * FROM (SELECT * FROM table OFFSET 5 ROWS LIMIT 10) LIMIT 10 OFFSET 5 ROWS;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(10, parsed.getLimit());
        assertEquals(5, parsed.getOffset());
        final var nested = (Query) parsed.getSources().getFirst();
        assertEquals(10, nested.getLimit());
        assertEquals(5, nested.getOffset());
    }
}
