package com.ecwid.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}
