package com.ecwid.parser;

import com.ecwid.parser.fragment.clause.*;
import com.ecwid.parser.fragment.enity.Column;
import com.ecwid.parser.fragment.enity.Query;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.ecwid.parser.fragment.clause.WhereClause.ClauseType.*;
import static com.ecwid.parser.fragment.clause.WhereClause.Operator.*;
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
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", LESS_THAN, ConstantOperand.class, "1", clause);
        }

        @Test
        @DisplayName("HAVING with constant")
        void simpleHaving() throws Exception {
            final var sql = "SELECT * FROM table HAVING id >= 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(HAVING, Column.class, "id", GREATER_THAN_OR_EQUALS, ConstantOperand.class, "1", clause);
        }

        @Test
        @DisplayName("with special chars in string")
        void specialCharsInString() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 'special;chars(,)in.string';";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
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
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", IN, ConstantListOperand.class, List.of("'1'", "'2'", "'3'"), clause);
        }

        @Test
        @DisplayName("of numbers")
        void listOfNumbers() throws Exception {
            final var sql = "SELECT * FROM table WHERE id in (1, 2, 3);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", IN, ConstantListOperand.class, List.of("1", "2", "3"), clause);
        }
    }

    @TestFactory
    @DisplayName("various operators")
    Stream<DynamicTest> variousOperators() {
        return Arrays.stream(WhereClause.Operator.values()).map(operator -> {
            final var sql = "SELECT * FROM table WHERE id " + operator.getFullLexeme() + " 1;";
            final var rightType = operator.equals(WhereClause.Operator.IN) ? ConstantListOperand.class : ConstantOperand.class;
            final var rightValue = operator.equals(WhereClause.Operator.IN) ? List.of("1") : "1";
            return DynamicTest.dynamicTest(operator.name(), () -> {
                final var parsed = sqlParser.parse(sql);
                assertEquals(1, parsed.getWhereClauses().size());
                final var clause = parsed.getWhereClauses().getFirst();
                assertClauseEquals(WHERE, Column.class, "id", operator, rightType, rightValue, clause);
            });
        });
    }


    @Nested
    @DisplayName("nested one level query")
    class NestedOneLevelQuery {

        @Test
        @DisplayName("with basic select")
        void oneLevelBasicSelect() throws Exception {
            final var sql = """
                    select *
                    from users
                    where id in (select user_id from participants);
                    """;
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", IN, Query.class, true, clause);
        }

        @Test
        @DisplayName("with own WHERE")
        void oneLevelNestedCondition() throws Exception {
            final var sql = """
                    select *
                    from users
                    where id in (select user_id from participants where id = 'a');
                    """;
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", IN, Query.class, true, clause);
            final var nestedClause = (Query) clause.getRightOperand();
            assertEquals(1, nestedClause.getWhereClauses().size());
            final var nestedWhere = nestedClause.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", EQUALS, ConstantOperand.class, "'a'", nestedWhere);
        }

        @Test
        @DisplayName("with own WHERE before constant")
        void oneLevelNestedConditionAndConstant() throws Exception {
            final var sql = """
                    select *
                    from users
                    where id in (select user_id from participants where id = 'a')
                       or id = 2;
                    """;
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getWhereClauses().size());
            final var firstClause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", IN, Query.class, true, firstClause);
            final var secondClause = parsed.getWhereClauses().getLast();
            assertClauseEquals(OR, Column.class, "id", EQUALS, ConstantOperand.class, "2", secondClause);
        }

        @Test
        @DisplayName("with own WHERE after constant")
        void constantAndOneLevelNestedCondition() throws Exception {
            final var sql = """
                    select *
                    from users
                    where id = 2 AND
                    id in (select user_id from participants where id = 'a')
                    """;
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getWhereClauses().size());
            final var firstClause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, Column.class, "id", EQUALS, ConstantOperand.class, "2", firstClause);
            final var secondClause = parsed.getWhereClauses().getLast();
            assertClauseEquals(AND, Column.class, "id", IN, Query.class, true, secondClause);

        }
    }

    private void assertClauseEquals(
            WhereClause.ClauseType type,
            Class<? extends Operand> leftType,
            Object leftVal,
            WhereClause.Operator operator,
            Class<? extends Operand> rightType,
            Object rightVal,
            WhereClause actual) {
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
            case Column o -> o.getName();
            case ConstantOperand o -> o.getValue();
//                case Query o -> o.getQuery();
            case Query o -> true;
            case ConstantListOperand o -> o.getValues();
            default ->
                    throw new IllegalArgumentException("Operand type not supported: " + operand.getClass().getSimpleName());
        };
    }
}
