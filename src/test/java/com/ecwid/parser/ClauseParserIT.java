package com.ecwid.parser;

import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.ConstantListOperand;
import com.ecwid.parser.fragment.ConstantOperand;
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


@DisplayName("When clause include")
public class ClauseParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("basic clause")
    class Basic {
        @Test
        @DisplayName("WHERE with constant")
        void simpleWhere() throws Exception {
            final var sql = "SELECT * FROM table WHERE id < 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", LESS_THAN, ConstantOperand.class, "1", clause);
        }

        @Test
        @DisplayName("HAVING with constant")
        void simpleHaving() throws Exception {
            final var sql = "SELECT * FROM table HAVING id >= 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(HAVING, Column.class, "id", GREATER_THAN_OR_EQUALS, ConstantOperand.class, "1", clause);
        }

        @Test
        @DisplayName("with special chars in string")
        void specialCharsInString() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 'special;chars(,)in.string';";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, ConstantOperand.class, "'special;chars(,)in.string'", clause);
        }

        @Test
        @DisplayName("with function call")
        void functionCall() throws Exception {
            final var sql = "SELECT a, b FROM table HAVING COUNT(*) > 10;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(HAVING, Column.class, "COUNT(*)", GREATER_THAN, ConstantOperand.class, "10", clause);
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
            assertConditionEquals(WHERE, Column.class, "id", IN, ConstantListOperand.class, List.of("'1'", "'2'", "'3'"), clause);
        }

        @Test
        @DisplayName("of numbers")
        void listOfNumbers() throws Exception {
            final var sql = "SELECT * FROM table WHERE id in (1, 2, 3);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertConditionEquals(WHERE, Column.class, "id", IN, ConstantListOperand.class, List.of("1", "2", "3"), clause);
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
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, ConstantOperand.class, "'a'", nestedWhere);
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
            assertConditionEquals(OR, Column.class, "id", EQUALS, ConstantOperand.class, "2", secondClause);
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
            assertConditionEquals(WHERE, Column.class, "id", EQUALS, ConstantOperand.class, "2", firstClause);
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
            assertConditionEquals(WHERE, Query.class, nested, GREATER_THAN, ConstantOperand.class, "3", clause);
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
            final var rightType = operator.equals(Condition.Operator.IN) ? ConstantListOperand.class : ConstantOperand.class;
            final var rightValue = operator.equals(Condition.Operator.IN) ? List.of("1") : "1";
            return DynamicTest.dynamicTest(operator.name(), () -> {
                final var parsed = sqlParser.parse(sql);
                assertEquals(1, parsed.getFilters().size());
                final var clause = parsed.getFilters().getFirst();
                assertConditionEquals(WHERE, Column.class, "id", operator, rightType, rightValue, clause);
            });
        });
    }
}
