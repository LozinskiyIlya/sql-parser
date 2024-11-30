package com.ecwid.parser;

import com.ecwid.parser.fragment.clause.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ecwid.parser.fragment.clause.WhereClause.ClauseType.*;
import static com.ecwid.parser.fragment.clause.WhereClause.Operator.EQUALS;
import static com.ecwid.parser.fragment.clause.WhereClause.Operator.IN;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("When clause include")
public class ClauseParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("basic clause")
    class Basic {
        @Test
        @DisplayName("WHERE with constant")
        void simpleWhere() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, ColumnOperand.class, "id", EQUALS, ConstantOperand.class, "1", clause);
        }

        @Test
        @DisplayName("HAVING with constant")
        void simpleHaving() throws Exception {
            final var sql = "SELECT * FROM table HAVING id = 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(HAVING, ColumnOperand.class, "id", EQUALS, ConstantOperand.class, "1", clause);
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
            assertClauseEquals(WHERE, ColumnOperand.class, "id", IN, ListOperand.class, List.of("'1'", "'2'", "'3'"), clause);
        }

        @Test
        @DisplayName("of numbers")
        void listOfNumbers() throws Exception {
            final var sql = "SELECT * FROM table WHERE id in (1, 2, 3);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, ColumnOperand.class, "id", IN, ListOperand.class, List.of("1", "2", "3"), clause);
        }
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
            assertClauseEquals(WHERE, ColumnOperand.class, "id", IN, QueryOperand.class, true, clause);
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
            assertClauseEquals(WHERE, ColumnOperand.class, "id", IN, QueryOperand.class, true, clause);
            final var nestedClause = ((QueryOperand) clause.getRightOperand()).getQuery();
            assertEquals(1, nestedClause.getWhereClauses().size());
            final var nestedWhere = nestedClause.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, ColumnOperand.class, "id", EQUALS, ConstantOperand.class, "'a'", nestedWhere);
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
            assertClauseEquals(WHERE, ColumnOperand.class, "id", IN, QueryOperand.class, true, firstClause);
            final var secondClause = parsed.getWhereClauses().getLast();
            assertClauseEquals(OR, ColumnOperand.class, "id", EQUALS, ConstantOperand.class, "2", secondClause);
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
            assertClauseEquals(WHERE, ColumnOperand.class, "id", EQUALS, ConstantOperand.class, "2", firstClause);
            final var secondClause = parsed.getWhereClauses().getLast();
            assertClauseEquals(AND, ColumnOperand.class, "id", IN, QueryOperand.class, true, secondClause);

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
//          assertEquals(operator, clause.getOperator(), "Operator mismatch");

        final var leftOperand = actual.getLeftOperand();
        final var rightOperand = actual.getRightOperand();
        assertEquals(leftType, leftOperand.getClass(), "Left operand type mismatch");
        assertEquals(leftVal, getOperandValue(leftOperand), "Left operand value mismatch");
        assertEquals(rightType, rightOperand.getClass(), "Right operand type mismatch");
        assertEquals(rightVal, getOperandValue(rightOperand), "Right operand value mismatch");
    }


    private Object getOperandValue(Operand operand) {
        return switch (operand) {
            case ColumnOperand o -> o.getColumn();
            case ConstantOperand o -> o.getValue();
//                case QueryOperand o -> o.getQuery();
            case QueryOperand o -> true;
            case ListOperand o -> o.getValues();
            default ->
                    throw new IllegalArgumentException("Operand type not supported: " + operand.getClass().getSimpleName());
        };
    }
}
