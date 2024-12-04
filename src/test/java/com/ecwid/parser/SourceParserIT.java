package com.ecwid.parser;

import com.ecwid.parser.fragment.enity.Aliasable;
import com.ecwid.parser.fragment.enity.Nameable;
import com.ecwid.parser.fragment.enity.Query;
import com.ecwid.parser.fragment.enity.Table;
import com.ecwid.parser.fragment.source.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@DisplayName("When sources include")
public class SourceParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("table sources with")
    class TableSource {
        @Test
        @DisplayName("one table")
        void oneTable() throws Exception {
            final var sql = "SELECT * FROM some_table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSources().size());
            assertSourceEquals(Table.class, "some_table", null, parsed.getSources().getFirst());
        }

        @Test
        @DisplayName("table with alias")
        void tableWithAlias() throws Exception {
            final var sql = "SELECT * FROM some_table t;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSources().size());
            assertSourceEquals(Table.class, "some_table", "t", parsed.getSources().getFirst());
        }

        @Test
        @DisplayName("table with AS alias")
        void tableWithAsAlias() throws Exception {
            final var sql = "SELECT * FROM some_table as t;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSources().size());
            assertSourceEquals(Table.class, "some_table", "t", parsed.getSources().getFirst());
        }

        @Test
        @DisplayName("implicit cross join")
        void implicitCrossJoin() throws Exception {
            final var sql = "SELECT * FROM some_table, one_more_t, even_third_t;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getSources().size());
            assertSourceEquals(Table.class, "some_table", null, parsed.getSources().get(0));
            assertSourceEquals(Table.class, "one_more_t", null, parsed.getSources().get(1));
            assertSourceEquals(Table.class, "even_third_t", null, parsed.getSources().get(2));
        }
    }

    @Nested
    @DisplayName("nested query with")
    class NestedQuery {
        @Test
        @DisplayName("one level nested source")
        void oneLevelNestedSource() throws Exception {
            final var sql = "select * from (select * from some_table)";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSources().size());
            final var source = parsed.getSources().getFirst();
            assertSourceEquals(Query.class, null, null, source);
            final var nestedQuery = (Query) source;
            assertEquals(1, nestedQuery.getColumns().size());
            assertEquals("*", nestedQuery.getColumns().getFirst().name());
            assertEquals(1, nestedQuery.getSources().size());
            assertSourceEquals(Table.class, "some_table", null, nestedQuery.getSources().getFirst());
        }

        @Test
        @DisplayName("one level nested source with alias")
        void oneLevelNestedSourceWithAlias() throws Exception {
            final var sql = "SELECT * FROM (SELECT * FROM some_table) t";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSources().size());
            final var source = parsed.getSources().getFirst();
            assertSourceEquals(Query.class, null, "t", source);
            final var nestedQuery = (Query) source;
            assertEquals(1, nestedQuery.getColumns().size());
            assertEquals("*", nestedQuery.getColumns().getFirst().name());
            assertEquals(1, nestedQuery.getSources().size());
            assertSourceEquals(Table.class, "some_table", null, nestedQuery.getSources().getFirst());
        }

        @Test
        @DisplayName("one level nested source with AS alias")
        void oneLevelNestedSourceWithASAlias() throws Exception {
            final var sql = "SELECT * FROM (SELECT * FROM some_table) AS t";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSources().size());
            final var source = parsed.getSources().getFirst();
            assertSourceEquals(Query.class, null, "t", source);
            final var nestedQuery = (Query) source;
            assertEquals(1, nestedQuery.getColumns().size());
            assertEquals("*", nestedQuery.getColumns().getFirst().name());
            assertEquals(1, nestedQuery.getSources().size());
            assertSourceEquals(Table.class, "some_table", null, nestedQuery.getSources().getFirst());
        }

        @Test
        @DisplayName("mixed sources and multiple level nesting")
        void mixedSources() throws IOException {
            final var sql = """
                    SELECT * FROM a, b as c, d,
                        (SELECT * FROM
                                (SELECT * FROM e) AS f, g
                        ) h, i j, k as l,
                        (SELECT * FROM m) as n, o;
                    """;
            final var parsed = sqlParser.parse(sql);
            assertEquals(8, parsed.getSources().size());
            assertSourceEquals(Table.class, "a", null, parsed.getSources().get(0));
            assertSourceEquals(Table.class, "b", "c", parsed.getSources().get(1));
            assertSourceEquals(Table.class, "d", null, parsed.getSources().get(2));
            assertSourceEquals(Query.class, null, "h", parsed.getSources().get(3));

            final var nestedFirst = (Query) parsed.getSources().get(3);
            assertEquals(2, nestedFirst.getSources().size());
            assertSourceEquals(Query.class, null, "f", nestedFirst.getSources().getFirst());
            assertSourceEquals(Table.class, "g", null, nestedFirst.getSources().getLast());

            final var nestedNested = (Query) nestedFirst.getSources().getFirst();
            assertEquals(1, nestedNested.getSources().size());
            assertSourceEquals(Table.class, "e", null, nestedNested.getSources().getFirst());

            assertSourceEquals(Table.class, "i", "j", parsed.getSources().get(4));
            assertSourceEquals(Table.class, "k", "l", parsed.getSources().get(5));
            assertSourceEquals(Query.class, null, "n", parsed.getSources().get(6));

            final var nestedSecond = (Query) parsed.getSources().get(6);
            assertEquals(1, nestedSecond.getSources().size());
            assertSourceEquals(Table.class, "m", null, nestedSecond.getSources().getFirst());

            assertSourceEquals(Table.class, "o", null, parsed.getSources().get(7));
        }
    }

    private void assertSourceEquals(Class<? extends Source> expectedClass, String expectedName, String expectedAlias, Source actual) {
        assertEquals(expectedClass, actual.getClass());
        assertEquals(expectedAlias, actual.alias());
        if (actual instanceof Nameable nameable) {
            assertEquals(expectedName, nameable.name());
        } else {
            assertNull(expectedName);
        }
    }
}
