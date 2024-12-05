package com.ecwid.parser;

import com.ecwid.parser.fragment.clause.*;
import com.ecwid.parser.fragment.domain.Column;
import com.ecwid.parser.fragment.domain.Query;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.ecwid.parser.fragment.clause.Condition.ClauseType.*;
import static com.ecwid.parser.fragment.clause.Condition.Operator.*;
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
            assertClauseEquals(WHERE, Column.class, "id", LESS_THAN, ConstantOperand.class, "1", clause);
        }

        @Test
        @DisplayName("HAVING with constant")
        void simpleHaving() throws Exception {
            final var sql = "SELECT * FROM table HAVING id >= 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertClauseEquals(HAVING, Column.class, "id", GREATER_THAN_OR_EQUALS, ConstantOperand.class, "1", clause);
        }

        @Test
        @DisplayName("with special chars in string")
        void specialCharsInString() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 'special;chars(,)in.string';";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", EQUALS, ConstantOperand.class, "'special;chars(,)in.string'", clause);
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
            assertClauseEquals(WHERE, Column.class, "id", IN, ConstantListOperand.class, List.of("'1'", "'2'", "'3'"), clause);
        }

        @Test
        @DisplayName("of numbers")
        void listOfNumbers() throws Exception {
            final var sql = "SELECT * FROM table WHERE id in (1, 2, 3);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFilters().size());
            final var clause = parsed.getFilters().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", IN, ConstantListOperand.class, List.of("1", "2", "3"), clause);
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
            assertClauseEquals(WHERE, Column.class, "id", IN, Query.class, nested, clause);
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
            assertClauseEquals(WHERE, Column.class, "id", IN, Query.class, nested, clause);
            final var nestedClause = (Query) clause.getRightOperand();
            assertEquals(1, nestedClause.getFilters().size());
            final var nestedWhere = nestedClause.getFilters().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", EQUALS, ConstantOperand.class, "'a'", nestedWhere);
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
            assertClauseEquals(WHERE, Column.class, "id", IN, Query.class, nested, firstClause);
            final var secondClause = parsed.getFilters().getLast();
            assertClauseEquals(OR, Column.class, "id", EQUALS, ConstantOperand.class, "2", secondClause);
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
            assertClauseEquals(WHERE, Column.class, "id", EQUALS, ConstantOperand.class, "2", firstClause);
            final var secondClause = parsed.getFilters().getLast();
            assertClauseEquals(AND, Column.class, "id", IN, Query.class, nested, secondClause);
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
            assertClauseEquals(WHERE, Query.class, nested.toLowerCase(), GREATER_THAN, ConstantOperand.class, "3", clause);
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
            assertClauseEquals(WHERE, Query.class, nestedLeft.toLowerCase(), EQUALS, Query.class, nestedRight.toLowerCase(), clause);
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
                assertClauseEquals(WHERE, Column.class, "id", operator, rightType, rightValue, clause);
            });
        });
    }

    private void assertClauseEquals(
            Condition.ClauseType type,
            Class<? extends Operand> leftType,
            Object leftVal,
            Condition.Operator operator,
            Class<? extends Operand> rightType,
            Object rightVal,
            Condition actual) {
        assertEquals(type, actual.getClauseType(), "Clause type mismatch");
        assertEquals(operator, actual.getOperator(), "Operator mismatch");
        final var leftOperand = actual.getLeftOperand();
        final var rightOperand = actual.getRightOperand();
        assertEquals(leftType, leftOperand.getClass(), "Left operand type mismatch");
        assertEquals(leftVal, getOperandValue(leftOperand), "Left operand value mismatch");
        assertEquals(rightType, rightOperand.getClass(), "Right operand type mismatch");
        assertEquals(rightVal, getOperandValue(rightOperand), "Right operand value mismatch");
    }


    private Object getOperandValue(Operand operand) {
        return switch (operand) {
            case Column o -> o.name();
            case ConstantOperand o -> o.getValue();
            case Query o -> o.toString();
            case ConstantListOperand o -> o.getValues();
            default ->
                    throw new IllegalArgumentException("Operand type not supported: " + operand.getClass().getSimpleName());
        };
    }
}
