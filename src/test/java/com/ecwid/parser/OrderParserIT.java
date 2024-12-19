package com.ecwid.parser;

import com.ecwid.parser.fragment.Column;
import com.ecwid.parser.fragment.Constant;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.Sort;
import com.ecwid.parser.fragment.Sort.Direction;
import com.ecwid.parser.fragment.Sort.Nulls;
import com.ecwid.parser.fragment.domain.Fragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.ecwid.parser.fragment.Sort.Direction.ASC;
import static com.ecwid.parser.fragment.Sort.Direction.DESC;
import static com.ecwid.parser.fragment.Sort.Nulls.FIRST;
import static com.ecwid.parser.fragment.Sort.Nulls.LAST;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("When orderings include")
public class OrderParserIT extends AbstractSpringParserTest {


    @Nested
    @DisplayName("basic ORDER BY with")
    class Basic {
        @Test
        @DisplayName("single column")
        void singleColumn() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, LAST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("multiple columns")
        void multipleColumns() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id, salary;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, LAST, parsed.getSorts().getFirst());
            assertSortEquals(Column.class, "salary", ASC, LAST, parsed.getSorts().getLast());
        }

        @Test
        @DisplayName("column index")
        void columnIndex() throws IOException {
            final var sql = "SELECT employee_id, department_id, hire_date FROM employees ORDER BY 2;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Constant.class, "2", ASC, LAST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("constant values")
        void constantValues() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY 100, 22;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getSorts().size());
            assertSortEquals(Constant.class, "100", ASC, LAST, parsed.getSorts().getFirst());
            assertSortEquals(Constant.class, "22", ASC, LAST, parsed.getSorts().getLast());
        }

        @Test
        @DisplayName("function")
        void function() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY LOWER(employee_name);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "LOWER(employee_name)", ASC, LAST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("nested queries")
        void nestedQueries() throws IOException {
            final var nested = "SELECT AVG(salary) FROM salaries WHERE salaries.department_id = employees.department_id";
            final var sql = """
                    SELECT employee_id
                    FROM employees
                    ORDER BY (%s);
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Query.class, nested, ASC, LAST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("followed by other clauses")
        void followedByClauses() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id LIMIT 10;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, LAST, parsed.getSorts().getFirst());
            assertEquals(10, parsed.getLimit());
        }
    }

    @Nested
    @DisplayName("sorting configurations")
    class Configurations {
        @Test
        @DisplayName("column with ASC")
        void columnWithAsc() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id ASC;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, LAST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("column with DESC")
        void columnWithDesc() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id DESC;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", DESC, LAST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("column with NULLS FIRST")
        void columnWithNullsFirst() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id NULLS FIRST;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, FIRST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("column with NULLS LAST")
        void columnWithNullsLast() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id NULLS LAST;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, LAST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("column with DESC and NULLS LAST")
        void columnWithDescAndNullsLast() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id DESC NULLS LAST;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", DESC, LAST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("column with ASC and NULLS FIRST")
        void columnWithAscAndNullsFirst() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id ASC NULLS FIRST;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, FIRST, parsed.getSorts().getFirst());
        }

        @Test
        @DisplayName("multiple columns with combinations")
        void multipleColumnsWithCombinations() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id DESC NULLS LAST, salary ASC NULLS FIRST;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", DESC, LAST, parsed.getSorts().getFirst());
            assertSortEquals(Column.class, "salary", ASC, FIRST, parsed.getSorts().getLast());
        }

        @Test
        @DisplayName("multiple columns without config in between")
        void multipleColumnsWithoutConfigInBetween() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id NULLS FIRST, salary, age DESC;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, FIRST, parsed.getSorts().get(0));
            assertSortEquals(Column.class, "salary", ASC, LAST, parsed.getSorts().get(1));
            assertSortEquals(Column.class, "age", DESC, LAST, parsed.getSorts().get(2));
        }

        @Test
        @DisplayName("multiple columns with config in between")
        void multipleColumnsWithConfigInBetween() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id, salary DESC NULLS FIRST, age;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, LAST, parsed.getSorts().get(0));
            assertSortEquals(Column.class, "salary", DESC, FIRST, parsed.getSorts().get(1));
            assertSortEquals(Column.class, "age", ASC, LAST, parsed.getSorts().get(2));
        }

        @Test
        @DisplayName("followed by other clauses")
        void followedByOtherClauses() throws IOException {
            final var sql = "SELECT * FROM employees ORDER BY department_id ASC NULLS LAST LIMIT 10 OFFSET 5;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getSorts().size());
            assertSortEquals(Column.class, "department_id", ASC, LAST, parsed.getSorts().getFirst());
            assertEquals(10, parsed.getLimit());
            assertEquals(5, parsed.getOffset());
        }
    }


    @Test
    @DisplayName("with all that beauty")
    void withAllThatBeauty() throws IOException {
        final var nested = "SELECT department_id FROM departments WHERE employees.department_id = departments.id";
        final var sql = """
                SELECT employee_id, department_id, hire_date, salary
                FROM employees
                ORDER BY department_id NULLS LAST, 4 ASC NULLS FIRST, LOWER(employee_name),
                (%s), hire_date DESC;
                """.formatted(nested);
        final var parsed = sqlParser.parse(sql);
        assertEquals(5, parsed.getSorts().size());
        assertSortEquals(Column.class, "department_id", ASC, LAST, parsed.getSorts().get(0));
        assertSortEquals(Constant.class, "4", ASC, FIRST, parsed.getSorts().get(1));
        assertSortEquals(Column.class, "lower(employee_name)", ASC, LAST, parsed.getSorts().get(2));
        assertSortEquals(Query.class, nested, ASC, LAST, parsed.getSorts().get(3));
        assertSortEquals(Column.class, "hire_date", DESC, LAST, parsed.getSorts().get(4));
    }

    private void assertSortEquals(Class<? extends Fragment> type, String value, Direction direction, Nulls nulls, Sort actual) {
        assertFragmentEquals(type, value, null, actual.getSortBy());
        assertEquals(direction, actual.getDirection(), "Direction mismatch");
        assertEquals(nulls, actual.getNulls(), "Nulls mismatch");
    }
}
