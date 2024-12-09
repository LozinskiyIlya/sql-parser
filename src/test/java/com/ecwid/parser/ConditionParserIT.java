package com.ecwid.parser;

import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.ConstantList;
import com.ecwid.parser.fragment.Constant;
import com.ecwid.parser.fragment.Column;
import com.ecwid.parser.fragment.Query;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.ecwid.parser.fragment.Condition.ClauseType.*;
import static com.ecwid.parser.fragment.Condition.Operator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("When conditions include")
public class ConditionParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("basic clause")
    class Basic {
        @Test
        @DisplayName("WHERE with constant")
        void simpleWhere() throws Exception {
            final var sql = "SELECT * FROM table WHERE id != 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", NOT_EQUALS, Constant.class, "1", clause);
        }

        @Test
        @DisplayName("HAVING with constant")
        void simpleHaving() throws Exception {
            final var sql = "SELECT * FROM table HAVING id >= 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(HAVING, Column.class, "id", GREATER_THAN_OR_EQUALS, Constant.class, "1", clause);
        }

        @Test
        @DisplayName("with special chars in string")
        void specialCharsInString() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 'special;chars(,)in.string';";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, Constant.class, "'special;chars(,)in.string'", clause);
        }

        @Test
        @DisplayName("with function call")
        void functionCall() throws Exception {
            final var sql = "SELECT a, b FROM table HAVING COUNT(*) > 10;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(HAVING, Column.class, "COUNT(*)", GREATER_THAN, Constant.class, "10", clause);
        }
    }

    @Nested
    @DisplayName("multiple clauses with")
    class MultipleClauses {

        @Test
        @DisplayName("AND")
        void withAnd() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 1 AND name = 'john';";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, Constant.class, "1", firstClause);
            final var secondClause = parsed.getFilters().getLast();
            assertConditionEquals(AND, Column.class, "name", EQUALS, Constant.class, "'john'", secondClause);
        }

        @Test
        @DisplayName("OR")
        void withOr() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 1 OR name = 'john';";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, Constant.class, "1", firstClause);
            final var secondClause = parsed.getFilters().getLast();
            assertConditionEquals(OR, Column.class, "name", EQUALS, Constant.class, "'john'", secondClause);
        }

        @Test
        @DisplayName("AND & OR")
        void withAndOr() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 1 AND name = 'john' OR age = 30;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, Constant.class, "1", firstClause);
            final var secondClause = parsed.getFilters().get(1);
            assertConditionEquals(AND, Column.class, "name", EQUALS, Constant.class, "'john'", secondClause);
            final var thirdClause = parsed.getFilters().getLast();
            assertConditionEquals(OR, Column.class, "age", EQUALS, Constant.class, "30", thirdClause);
        }

        @Test
        @DisplayName("OR & AND")
        void withOrAnd() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 1 OR name = 'john' AND age = 30;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, Constant.class, "1", firstClause);
            final var secondClause = parsed.getFilters().get(1);
            assertConditionEquals(OR, Column.class, "name", EQUALS, Constant.class, "'john'", secondClause);
            final var thirdClause = parsed.getFilters().getLast();
            assertConditionEquals(AND, Column.class, "age", EQUALS, Constant.class, "30", thirdClause);
        }

        @Test
        @DisplayName("mixed AND & OR")
        void mixed() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 1 OR name = 'john' AND age = 30 OR last_name = 'doe' AND status LIKE '%_active_%' ;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(5, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().get(0);
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, Constant.class, "1", firstClause);
            final var secondClause = parsed.getFilters().get(1);
            assertConditionEquals(OR, Column.class, "name", EQUALS, Constant.class, "'john'", secondClause);
            final var thirdClause = parsed.getFilters().get(2);
            assertConditionEquals(AND, Column.class, "age", EQUALS, Constant.class, "30", thirdClause);
            final var fourthClause = parsed.getFilters().get(3);
            assertConditionEquals(OR, Column.class, "last_name", EQUALS, Constant.class, "'doe'", fourthClause);
            final var fifthClause = parsed.getFilters().get(4);
            assertConditionEquals(AND, Column.class, "status", LIKE, Constant.class, "'%_active_%'", fifthClause);
        }

        @Test
        @DisplayName("AND & OR with brackets")
        void withAndOrBrackets() throws Exception {
            final var sql = """
                    SELECT *
                    FROM employees
                    WHERE (department = 'engineering' AND salary > 50000)
                    OR (department = 'sales' AND salary > 40000)""";
            final var parsed = sqlParser.parse(sql);
            assertEquals(4, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().get(0);
            assertConditionEquals(WHERE, Column.class, "department", EQUALS, Constant.class, "'engineering'", firstClause);
            final var secondClause = parsed.getFilters().get(1);
            assertConditionEquals(AND, Column.class, "salary", GREATER_THAN, Constant.class, "50000", secondClause);
            final var thirdClause = parsed.getFilters().get(2);
            assertConditionEquals(OR, Column.class, "department", EQUALS, Constant.class, "'sales'", thirdClause);
            final var fourthClause = parsed.getFilters().get(3);
            assertConditionEquals(AND, Column.class, "salary", GREATER_THAN, Constant.class, "40000", fourthClause);
        }
    }

    @Nested
    @DisplayName("list clause")
    class ListClause {
        @Test
        @DisplayName("of strings")
        void listOfStrings() throws Exception {
            final var sql = "SELECT * FROM table WHERE id in ('1', '2', '3');";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", IN, ConstantList.class, List.of("'1'", "'2'", "'3'"), clause);
        }

        @Test
        @DisplayName("of numbers")
        void listOfNumbers() throws Exception {
            final var sql = "SELECT * FROM table WHERE id in (1, 2, 3);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", IN, ConstantList.class, List.of("1", "2", "3"), clause);
        }

        @Test
        @DisplayName("between other conditions")
        void betweenOtherConditions() throws Exception {
            final var sql = "SELECT * FROM table WHERE (id = 1 OR id in (1, 2, 3)) AND name = 'john';";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, Constant.class, "1", firstClause);
            final var secondClause = parsed.getFilters().get(1);
            assertConditionEquals(OR, Column.class, "id", IN, ConstantList.class, List.of("1", "2", "3"), secondClause);
            final var thirdClause = parsed.getFilters().getLast();
            assertConditionEquals(AND, Column.class, "name", EQUALS, Constant.class, "'john'", thirdClause);
        }
    }

    @Nested
    @DisplayName("nested one level query")
    class NestedOneLevelQuery {

        @Test
        @DisplayName("with basic select")
        void oneLevelBasicSelect() throws Exception {
            final var nested = "select user_id from participants";
            final var sql = """
                    select *
                    from users
                    where id in (%s);
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", IN, Query.class, nested, clause);
        }

        @Test
        @DisplayName("with nested WHERE")
        void oneLevelNestedCondition() throws Exception {
            final var nested = "select user_id from participants where id = 'a'";
            final var sql = """
                    select *
                    from users
                    where id in (%s);
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", IN, Query.class, nested, clause);
            final var nestedClause = (Query) clause.getRightOperand();
            assertEquals(1, nestedClause.getFilters().size());
            final var nestedWhere = nestedClause.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, Constant.class, "'a'", nestedWhere);
        }

        @Test
        @DisplayName("with nested WHERE goes before constant condition")
        void oneLevelNestedConditionAndConstant() throws Exception {
            final var nested = "select user_id from participants where id = 'a'";
            final var sql = """
                    select *
                    from users
                    where id in (%s)
                       or id = 2;
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", IN, Query.class, nested, firstClause);
            final var secondClause = parsed.getFilters().getLast();
            assertConditionEquals(OR, Column.class, "id", EQUALS, Constant.class, "2", secondClause);
        }

        @Test
        @DisplayName("with nested WHERE goes after constant condition")
        void constantAndOneLevelNestedCondition() throws Exception {
            final var nested = "select user_id from participants where id = 'a'";
            final var sql = """
                    select *
                    from users
                    where id = 2 AND
                    id in (%s);
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, Constant.class, "2", firstClause);
            final var secondClause = parsed.getFilters().getLast();
            assertConditionEquals(AND, Column.class, "id", IN, Query.class, nested, secondClause);
        }

        @Test
        @DisplayName("as a first operand")
        void oneLevelNestedConditionAsFirstOperand() throws Exception {
            final var nested = "SELECT COUNT(*) FROM projects WHERE projects.employee_id = employees.id";
            final var sql = """
                    SELECT *
                    FROM employees
                    WHERE (%s) > 3;
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Query.class, nested, GREATER_THAN, Constant.class, "3", clause);
        }

        @Test
        @DisplayName("as a first operand in brackets")
        void oneLevelNestedConditionAsFirstOperandInBrackets() throws Exception {
            final var nested = "SELECT COUNT(*) FROM projects WHERE projects.employee_id = employees.id";
            final var sql = """
                    SELECT *
                    FROM employees
                    WHERE ((%s) > 3);
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            System.out.println(parsed);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Query.class, nested, GREATER_THAN, Constant.class, "3", clause);
        }

        @Test
        @DisplayName("as both operands")
        void oneLevelNestedConditionAsBothOperands() throws IOException {
            final var nestedLeft = "SELECT AVG(salary) FROM employees WHERE department_id = 1";
            final var nestedRight = "SELECT AVG(salary) FROM employees WHERE department_id = 2";
            final var sql = """
                    SELECT *
                    FROM employees
                    WHERE (%s) = (%s);
                    """.formatted(nestedLeft, nestedRight);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Query.class, nestedLeft, EQUALS, Query.class, nestedRight, clause);
        }

        @Test
        @DisplayName("as both operands with different operators")
        void asBothOperandsTwice() throws Exception {
            final var nestedLeft = "SELECT COUNT(*) FROM projects WHERE projects.employee_id = employees.id";
            final var nestedRight = "SELECT COUNT(*) FROM projects WHERE projects.employee_id = employees.id";
            final var sql = """
                    SELECT *
                    FROM employees
                    WHERE (%s) > (%s) AND
                    (%s) < (%s);
                    """.formatted(nestedLeft, nestedRight, nestedRight, nestedLeft);
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getFilters().size());
            final var firstClause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Query.class, nestedLeft, GREATER_THAN, Query.class, nestedRight, firstClause);
            final var secondClause = parsed.getFilters().getLast();
            assertConditionEquals(AND, Query.class, nestedRight, LESS_THAN, Query.class, nestedLeft, secondClause);
        }
    }

    @Nested
    @DisplayName("deep nesting with")
    class DeepNesting {

        @Test
        @DisplayName("2 levels and a function comparison")
        void twoLevelsAndFunctionComparison() throws Exception {
            final var nestedNested = "SELECT department, COUNT(*) FROM employees";
            final var nested = "SELECT AVG(count) FROM (%s) sub_query".formatted(nestedNested);
            final var sql = """
                    SELECT department, COUNT(*)
                    FROM employees
                    WHERE COUNT(*) > (%s);
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "count(*)", GREATER_THAN, Query.class, nested, clause);

        }
    }

    @TestFactory
    @DisplayName("various operators")
    Stream<DynamicTest> variousOperators() {
        return Arrays.stream(Condition.Operator.values()).map(operator -> {
            final var sql = "SELECT * FROM table WHERE id " + operator.getFullLexeme() + " 1;";
            return DynamicTest.dynamicTest(operator.name(), () -> {
                final var parsed = sqlParser.parse(sql);
                assertEquals(1, parsed.getFilters().size());
                final var clause = parsed.getFilters().getFirst();
                assertConditionEquals(WHERE, Column.class, "id", operator, Constant.class, "1", clause);
            });
        });
    }
}
