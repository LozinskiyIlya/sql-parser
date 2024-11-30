package com.ecwid.parser;

import com.ecwid.parser.fragment.enity.Query;
import com.ecwid.parser.fragment.enity.Table;
import com.ecwid.parser.fragment.source.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
            assertEquals(1, parsed.getFromSources().size());
            final var source = parsed.getFromSources().getFirst();
            assertEquals(Table.class, source.getClass());
            assertEquals("some_table", ((Table) source).getName());
        }

        @Test
        @DisplayName("table with alias")
        void tableWithAlias() throws Exception {
            final var sql = "SELECT * FROM some_table t;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFromSources().size());
            final var source = parsed.getFromSources().getFirst();
            assertEquals(Table.class, source.getClass());
            assertEquals("some_table", ((Table) source).getName());
            assertEquals("t", ((Table) source).getAlias());
        }
    }

    @Nested
    @DisplayName("nested query")
    class NestedQuery {
        @Test
        @DisplayName("One level nested source")
        void oneLevelNestedSource() throws Exception {
            final var sql = "select * from (select * from some_table)";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFromSources().size());
            final var source = parsed.getFromSources().getFirst();
            assertEquals(Query.class, source.getClass());
            final var nestedQuery = (Query) source;
            assertEquals(1, nestedQuery.getColumns().size());
            assertEquals("*", nestedQuery.getColumns().getFirst().getName());
            assertEquals(1, nestedQuery.getFromSources().size());
            final var nestedSource = nestedQuery.getFromSources().getFirst();
            assertEquals(Table.class, nestedSource.getClass());
            assertEquals("some_table", ((Table) nestedSource).getName());
            System.out.println(parsed);
        }
    }

    private void assertSourceEquals(Class<? extends Source> expectedClass, Object expectedValue, Source actual) {
        assertEquals(expectedClass, actual.getClass());
        assertEquals(expectedValue, ((Table) actual).getName());
    }
}
