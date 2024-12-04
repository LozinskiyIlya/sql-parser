package com.ecwid.parser;

import com.ecwid.parser.fragment.enity.Aliasable;
import com.ecwid.parser.fragment.enity.Nameable;
import com.ecwid.parser.fragment.enity.Query;
import com.ecwid.parser.fragment.enity.Table;
import com.ecwid.parser.fragment.source.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
            final var sql = "select * from (select * from some_table) as t";
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
