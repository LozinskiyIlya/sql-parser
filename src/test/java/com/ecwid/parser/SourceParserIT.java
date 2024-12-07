package com.ecwid.parser;

import com.ecwid.parser.fragment.domain.Nameable;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.Table;
import com.ecwid.parser.fragment.domain.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

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
            final var nested = "select * from some_table";
            final var sql = "select * from (%s)".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSources().size());
            final var source = parsed.getSources().getFirst();
            assertSourceEquals(Query.class, nested, null, source);
            final var nestedQuery = (Query) source;
            assertEquals(1, nestedQuery.getColumns().size());
            assertEquals("*", nestedQuery.getColumns().getFirst().getValue());
            assertEquals(1, nestedQuery.getSources().size());
            assertSourceEquals(Table.class, "some_table", null, nestedQuery.getSources().getFirst());
        }

        @Test
        @DisplayName("one level nested source with alias")
        void oneLevelNestedSourceWithAlias() throws Exception {
            final var nested = "SELECT * FROM some_table";
            final var sql = "SELECT * FROM (%s) t".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSources().size());
            final var source = parsed.getSources().getFirst();
            assertSourceEquals(Query.class, nested, "t", source);
            final var nestedQuery = (Query) source;
            assertEquals(1, nestedQuery.getColumns().size());
            assertEquals("*", nestedQuery.getColumns().getFirst().getValue());
            assertEquals(1, nestedQuery.getSources().size());
            assertSourceEquals(Table.class, "some_table", null, nestedQuery.getSources().getFirst());
        }

        @Test
        @DisplayName("one level nested source with AS alias")
        void oneLevelNestedSourceWithASAlias() throws Exception {
            final var nested = "SELECT * FROM some_table";
            final var sql = "SELECT * FROM (%s) AS t".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSources().size());
            final var source = parsed.getSources().getFirst();
            assertSourceEquals(Query.class, nested, "t", source);
            final var nestedQuery = (Query) source;
            assertEquals(1, nestedQuery.getColumns().size());
            assertEquals("*", nestedQuery.getColumns().getFirst().getValue());
            assertEquals(1, nestedQuery.getSources().size());
            assertSourceEquals(Table.class, "some_table", null, nestedQuery.getSources().getFirst());
        }

        @Test
        @DisplayName("one level nested surrounded by tables")
        void oneLevelNestedSourceSurroundedByTables() throws Exception {
            final var nested = "SELECT * FROM some_table";
            final var sql = "SELECT * FROM a, (%s), b".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getSources().size());
            assertSourceEquals(Table.class, "a", null, parsed.getSources().get(0));
            assertSourceEquals(Query.class, nested, null, parsed.getSources().get(1));
            assertSourceEquals(Table.class, "b", null, parsed.getSources().get(2));
        }
    }

    @Test
    @DisplayName("all that beauty")
    void withAllThatBeauty() throws IOException {
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
        assertSourceIsQuery("h", parsed.getSources().get(3));
        final var nestedFirst = (Query) parsed.getSources().get(3);
        assertEquals(2, nestedFirst.getSources().size());
        assertSourceIsQuery("f", nestedFirst.getSources().getFirst());
        assertSourceEquals(Table.class, "g", null, nestedFirst.getSources().getLast());
        final var nestedNested = (Query) nestedFirst.getSources().getFirst();
        assertEquals(1, nestedNested.getSources().size());
        assertSourceEquals(Table.class, "e", null, nestedNested.getSources().getFirst());
        assertSourceEquals(Table.class, "i", "j", parsed.getSources().get(4));
        assertSourceEquals(Table.class, "k", "l", parsed.getSources().get(5));
        assertSourceIsQuery("n", parsed.getSources().get(6));
        final var nestedSecond = (Query) parsed.getSources().get(6);
        assertEquals(1, nestedSecond.getSources().size());
        assertSourceEquals(Table.class, "m", null, nestedSecond.getSources().getFirst());
        assertSourceEquals(Table.class, "o", null, parsed.getSources().get(7));
    }

    private void assertSourceEquals(Class<? extends Source> expectedClass, String expectedVal, String expectedAlias, Source actual) {
        assertEquals(expectedClass, actual.getClass());
        assertEquals(expectedAlias, actual.getAlias());
        if (actual instanceof Nameable nameable) {
            assertEquals(expectedVal, nameable.getName());
        } else {
            final var alias = StringUtils.hasText(expectedAlias) ? " " + expectedAlias : "";
            assertEquals(expectedVal.toLowerCase() + alias, actual.toString());
        }
    }

    private void assertSourceIsQuery(String expectedAlias, Source actual) {
        assertEquals(Query.class, actual.getClass());
        assertEquals(expectedAlias, actual.getAlias());
    }
}
