package com.ecwid.parser;

import com.ecwid.parser.fragment.Column;
import com.ecwid.parser.fragment.Constant;
import com.ecwid.parser.fragment.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("When groupings include")
public class GroupParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("simple groupings with")
    class SimpleGrouping {

        @Test
        @DisplayName("one column")
        void oneColumn() throws IOException {
            final var sql = "SELECT * FROM table GROUP BY column;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getGroupings().size());
            final var grouping = parsed.getGroupings().getFirst();
            assertFragmentEquals(Column.class, "column", null, grouping);
        }

        @Test
        @DisplayName("constant")
        void constant() throws IOException {
            final var sql = "SELECT * FROM table GROUP BY 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getGroupings().size());
            final var grouping = parsed.getGroupings().getFirst();
            assertFragmentEquals(Constant.class, "1", null, grouping);
        }

        @Test
        @DisplayName("multiple columns")
        void multipleColumns() throws IOException {
            final var sql = "SELECT * FROM table GROUP BY column1, column2;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getGroupings().size());
            assertFragmentEquals(Column.class, "column1", null, parsed.getGroupings().get(0));
            assertFragmentEquals(Column.class, "column2", null, parsed.getGroupings().get(1));
        }

        @Test
        @DisplayName("functions")
        void functions() throws IOException {
            final var sql = "SELECT * FROM table GROUP BY YEAR(column), MONTH(column);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getGroupings().size());
            assertFragmentEquals(Column.class, "YEAR(column)", null, parsed.getGroupings().get(0));
            assertFragmentEquals(Column.class, "MONTH(column)", null, parsed.getGroupings().get(1));
        }

        @Test
        @DisplayName("mixed")
        void expressions() throws IOException {
            final var sql = "SELECT * FROM table GROUP BY 1, column, LOWER(column);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getGroupings().size());
            assertFragmentEquals(Constant.class, "1", null, parsed.getGroupings().get(0));
            assertFragmentEquals(Column.class, "column", null, parsed.getGroupings().get(1));
            assertFragmentEquals(Column.class, "LOWER(column)", null, parsed.getGroupings().get(2));
        }
    }

    @Nested
    @DisplayName("nested query")
    class NestedQuery {

        @Test
        @DisplayName("basic nested query in GROUP BY")
        void basicNestedQueryInGroupBy() throws IOException {
            final var nested = "SELECT department_id FROM departments WHERE employees.department_id = departments.id";
            final var sql = """
                    SELECT employee_id, COUNT(*)
                    FROM employees
                    GROUP BY (%s);
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getGroupings().size());
            assertFragmentEquals(Query.class, nested, null, parsed.getGroupings().getFirst());
        }

        @Test
        @DisplayName("nested with functions in GROUP BY")
        void nestedWithFunctionsInGroupBy() throws IOException {
            final var nested = "SELECT department_id FROM departments WHERE employees.department_id = departments.id";
            final var sql = """
                    SELECT department_id, COUNT(*)
                    FROM employees
                    GROUP BY (%s);""".formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getGroupings().size());
            assertFragmentEquals(Query.class, nested, null, parsed.getGroupings().getFirst());
        }

        @Test
        @DisplayName("nested query with mixed columns in GROUP BY")
        void nestedWithMixedColumnsInGroupBy() throws IOException {
            final var nested = "SELECT department_id FROM departments WHERE employees.department_id = departments.id";
            final var sql = """
                    SELECT employee_id, COUNT(*)
                    FROM employees
                    GROUP BY employee_id, (%s), department_id;
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getGroupings().size());
            assertFragmentEquals(Column.class, "employee_id", null, parsed.getGroupings().get(0));
            assertFragmentEquals(Query.class, nested, null, parsed.getGroupings().get(1));
            assertFragmentEquals(Column.class, "department_id", null, parsed.getGroupings().get(2));
        }
    }

    @Test
    @DisplayName("with all that beauty")
    void withAllThatBeauty() throws IOException {
        final var nested = "SELECT department_id FROM departments WHERE employees.department_id = departments.id";
        final var sql = """
                SELECT department_id, YEAR(hire_date),
                       (salary + bonus) AS total_compensation, COUNT(*)
                FROM employees
                GROUP BY department_id,
                         YEAR(hire_date),
                         (%s),
                         LOWER(job_title);
                """.formatted(nested);
        final var parsed = sqlParser.parse(sql);
        assertEquals(4, parsed.getGroupings().size());
        assertFragmentEquals(Column.class, "department_id", null, parsed.getGroupings().get(0));
        assertFragmentEquals(Column.class, "YEAR(hire_date)", null, parsed.getGroupings().get(1));
        assertFragmentEquals(Query.class, nested, null, parsed.getGroupings().get(2));
        assertFragmentEquals(Column.class, "LOWER(job_title)", null, parsed.getGroupings().get(3));
    }

}
