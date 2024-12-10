package com.ecwid.parser;

import com.ecwid.parser.fragment.*;
import com.ecwid.parser.fragment.Condition.ClauseType;
import com.ecwid.parser.fragment.Condition.Operator;
import com.ecwid.parser.fragment.domain.Source;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.ecwid.parser.fragment.Join.JoinType;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("When join includes")
public class JoinParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("one JOIN with")
    class OneJoin {

        @Test
        @DisplayName("just JOIN")
        void join() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON table1.id = table2.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table2", null, join);
            final var condition = join.getConditions().getFirst();
            assertConditionEquals(ClauseType.ON, Column.class, "table1.id", Operator.EQUALS, Column.class, "table2.id", condition);
        }

        @Test
        @DisplayName("alias")
        void joinWithAlias() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 t ON table1.id = t.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table2", "t", join);
            final var condition = join.getConditions().getFirst();
            assertConditionEquals(ClauseType.ON, Column.class, "table1.id", Operator.EQUALS, Column.class, "t.id", condition);
        }

        @Test
        @DisplayName("as alias")
        void joinWithAsAlias() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 as t ON table1.id = t.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table2", "t", join);
            final var condition = join.getConditions().getFirst();
            assertConditionEquals(ClauseType.ON, Column.class, "table1.id", Operator.EQUALS, Column.class, "t.id", condition);
        }

        @Test
        @DisplayName("condition in brackets")
        void joinWithConditionInBrackets() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON (table1.id = table2.id);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table2", null, join);
            final var condition = join.getConditions().getFirst();
            assertConditionEquals(ClauseType.ON, Column.class, "table1.id", Operator.EQUALS, Column.class, "table2.id", condition);
        }

        @Test
        @DisplayName("condition in brackets is not last")
        void joinWithConditionInBracketsIsNotLast() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON (table1.id = table2.id) or a = b;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table2", null, join);
            assertEquals(2, join.getConditions().size());
            final var condition = join.getConditions().getFirst();
            assertConditionEquals(ClauseType.ON, Column.class, "table1.id", Operator.EQUALS, Column.class, "table2.id", condition);
            final var condition1 = join.getConditions().getLast();
            assertConditionEquals(ClauseType.OR, Column.class, "a", Operator.EQUALS, Column.class, "b", condition1);
        }

        @Test
        @DisplayName("conditions in brackets with mandatory inner brackets")
        void joinWithConditionsInExcessiveBrackets() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON ((a = b or c != d) AND e NOT LIKE '%f');";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table2", null, join);
            assertEquals(3, join.getConditions().size());
            final var condition = join.getConditions().getFirst();
            assertConditionEquals(ClauseType.ON, Column.class, "a", Operator.EQUALS, Column.class, "b", condition);
            final var condition1 = join.getConditions().get(1);
            assertConditionEquals(ClauseType.OR, Column.class, "c", Operator.NOT_EQUALS, Column.class, "d", condition1);
            final var condition2 = join.getConditions().getLast();
            assertConditionEquals(ClauseType.AND, Column.class, "e", Operator.NOT_LIKE, Constant.class, "'%f'", condition2);
        }

        @Test
        @DisplayName("condition with function in brackets")
        void joinWithConditionWithFunctionInBrackets() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON (count(*) <= table2.id);";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table2", null, join);
            final var condition = join.getConditions().getFirst();
            assertConditionEquals(ClauseType.ON, Column.class, "count(*)", Operator.LESS_THAN_OR_EQUALS, Column.class, "table2.id", condition);
        }

        @Test
        @DisplayName("multiple conditions")
        void joinWithMultipleConditions() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON table1.id1 = table2.id1 AND table1.id2 >= 2;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table2", null, join);
            final var conditions = join.getConditions();
            assertEquals(2, conditions.size());
            assertConditionEquals(ClauseType.ON, Column.class, "table1.id1", Operator.EQUALS, Column.class, "table2.id1", conditions.get(0));
            assertConditionEquals(ClauseType.AND, Column.class, "table1.id2", Operator.GREATER_THAN_OR_EQUALS, Constant.class, "2", conditions.get(1));
        }

        @Test
        @DisplayName("function as first operand")
        void joinWithFunctionAsFirstOperand() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 t ON count(table1.*) = t.some_counter;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table2", "t", join);
            final var condition = join.getConditions().getFirst();
            assertConditionEquals(ClauseType.ON, Column.class, "count(table1.*)", Operator.EQUALS, Column.class, "t.some_counter", condition);
        }

        @Test
        @DisplayName("complex join type")
        void joinWithComplexType() throws Exception {
            final var sql = "SELECT * FROM table1 NATURAL FULL OUTER JOIN table2 ON table1.id = table2.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.NATURAL_FULL_OUTER, Table.class, "table2", null, join);
        }

        @Test
        @DisplayName("nested query in source")
        void nestedQueryInSource() throws Exception {
            final var nested = "SELECT id FROM table_c WHERE status = 'completed' AND amount > 50";
            final var sql = """
                    SELECT a.id, b.name
                    FROM table_a a
                    JOIN (%s) b ON a.id = b.id;
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Query.class, nested, "b", join);
            final var conditions = join.getConditions();
            assertEquals(1, conditions.size());
            assertConditionEquals(ClauseType.ON, Column.class, "a.id", Operator.EQUALS, Column.class, "b.id", conditions.getFirst());
        }

        @Test
        @DisplayName("nested query in condition")
        void nestedQueryInCondition() throws Exception {
            final var nested = "SELECT id FROM table_c WHERE status = 'completed' AND amount > 50 LIMIT 1";
            final var sql = """
                    SELECT a.id, a.name, b.status
                    FROM table_a a
                    JOIN table_b b
                        ON a.id IN (%s);
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "table_b", "b", join);
            final var conditions = join.getConditions();
            assertEquals(1, conditions.size());
            assertConditionEquals(ClauseType.ON, Column.class, "a.id", Operator.IN, Query.class, nested, conditions.getFirst());
        }


        @TestFactory
        @DisplayName("various join types")
        Stream<DynamicTest> variousJoinTypes() {
            return Arrays.stream(JoinType.values()).map(joinType -> {
                final var sql = "SELECT * FROM table " + joinType.getFullLexeme() + " table1 ON a = b;";
                return DynamicTest.dynamicTest(joinType.name(), () -> {
                    final var parsed = sqlParser.parse(sql);
                    assertEquals(1, parsed.getJoins().size());
                    final var join = parsed.getJoins().getFirst();
                    assertJoinEquals(joinType, Table.class, "table1", null, join);
                });
            });
        }
    }

    @Nested
    @DisplayName("multiple JOINs with")
    class MultipleJoins {

        @Test
        @DisplayName("different alias")
        void differentJAlias() throws Exception {
            final var sql = """
                    SELECT *
                    FROM a
                    JOIN b ON a.id = b.id
                    JOIN c d ON b.id = c.id
                    JOIN e as f ON c.id = f.id
                    JOIN g as h ON e.id = h.id
                    JOIN i j ON g.id = j.id
                    JOIN k ON j.id = k.id
                    """;
            final var parsed = sqlParser.parse(sql);
            assertEquals(6, parsed.getJoins().size());
            final var joins = parsed.getJoins();
            assertJoinEquals(JoinType.JOIN, Table.class, "b", null, joins.get(0));
            assertJoinEquals(JoinType.JOIN, Table.class, "c", "d", joins.get(1));
            assertJoinEquals(JoinType.JOIN, Table.class, "e", "f", joins.get(2));
            assertJoinEquals(JoinType.JOIN, Table.class, "g", "h", joins.get(3));
            assertJoinEquals(JoinType.JOIN, Table.class, "i", "j", joins.get(4));
            assertJoinEquals(JoinType.JOIN, Table.class, "k", null, joins.get(5));
        }

        @Test
        @DisplayName("different join type")
        void differentJoinType() throws Exception {
            final var sql = """
                    SELECT *
                    FROM table1
                    NATURAL FULL OUTER JOIN table2 ON table1.id = table2.id
                    LEFT JOIN table3 ON table2.id = table3.id;
                    """;
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getJoins().size());
            final var firstJoin = parsed.getJoins().getFirst();
            assertJoinEquals(JoinType.NATURAL_FULL_OUTER, Table.class, "table2", null, firstJoin);
            assertConditionEquals(ClauseType.ON, Column.class, "table1.id", Operator.EQUALS, Column.class, "table2.id", firstJoin.getConditions().getFirst());
            final var secondJoin = parsed.getJoins().getLast();
            assertJoinEquals(JoinType.LEFT, Table.class, "table3", null, secondJoin);
            assertConditionEquals(ClauseType.ON, Column.class, "table2.id", Operator.EQUALS, Column.class, "table3.id", secondJoin.getConditions().getFirst());
        }


        @Test
        @DisplayName("different conditions")
        void differentConditions() throws Exception {
            final var sql = """
                    SELECT *
                    FROM a
                    JOIN c ON b.id = c.id OR (a.id IN (1, 2))
                    LEFT JOIN d ON (a.id LIKE b.id) AND d.id IS NOT NULL
                    """;
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getJoins().size());
            final var joins = parsed.getJoins();
            final var firstJoin = joins.getFirst();
            assertJoinEquals(JoinType.JOIN, Table.class, "c", null, firstJoin);
            final var firstConditions = firstJoin.getConditions();
            assertEquals(2, firstConditions.size());
            assertConditionEquals(ClauseType.ON, Column.class, "b.id", Operator.EQUALS, Column.class, "c.id", firstConditions.get(0));
            assertConditionEquals(ClauseType.OR, Column.class, "a.id", Operator.IN, ConstantList.class, List.of("1", "2"), firstConditions.get(1));
            final var secondJoin = joins.getLast();
            assertJoinEquals(JoinType.LEFT, Table.class, "d", null, secondJoin);
            final var secondConditions = secondJoin.getConditions();
            assertEquals(2, secondConditions.size());
            assertConditionEquals(ClauseType.ON, Column.class, "a.id", Operator.LIKE, Column.class, "b.id", secondConditions.get(0));
            assertConditionEquals(ClauseType.AND, Column.class, "d.id", Operator.IS_NOT, Constant.class, "null", secondConditions.get(1));
        }

        @Test
        @DisplayName("nested queries")
        void nestedQueries() throws IOException {
            final var nested = "SELECT id FROM table_c WHERE status = 'completed' AND amount > 50";
            final var sql = """
                    SELECT a.id, b.name, c.status
                    FROM table_a a
                    JOIN table_b b
                    ON a.id = b.a_id
                    AND b.date > '2023-01-01'
                    JOIN table_c c
                    ON b.id = c.b_id
                    AND c.status = 'pending'
                    AND c.id IN (%s)
                    """.formatted(nested);
            final var parsed = sqlParser.parse(sql);
            final var joins = parsed.getJoins();
            assertEquals(2, joins.size());
            assertJoinEquals(JoinType.JOIN, Table.class, "table_b", "b", joins.get(0));
            assertJoinEquals(JoinType.JOIN, Table.class, "table_c", "c", joins.get(1));
            final var conditions = joins.get(0).getConditions();
            assertEquals(2, conditions.size());
            assertConditionEquals(ClauseType.ON, Column.class, "a.id", Operator.EQUALS, Column.class, "b.a_id", conditions.get(0));
            assertConditionEquals(ClauseType.AND, Column.class, "b.date", Operator.GREATER_THAN, Constant.class, "'2023-01-01'", conditions.get(1));
            final var conditions1 = joins.get(1).getConditions();
            assertEquals(3, conditions1.size());
            assertConditionEquals(ClauseType.ON, Column.class, "b.id", Operator.EQUALS, Column.class, "c.b_id", conditions1.get(0));
            assertConditionEquals(ClauseType.AND, Column.class, "c.status", Operator.EQUALS, Constant.class, "'pending'", conditions1.get(1));
            assertConditionEquals(ClauseType.AND, Column.class, "c.id", Operator.IN, Query.class, nested, conditions1.get(2));
        }

        @Test
        @DisplayName("nested join")
        void nestedJoin() throws Exception {
            final var sql = """
                    SELECT *
                    FROM a
                    JOIN (b JOIN c ON b.id = c.id) d
                    ON a.id = d.id;
                    """;
            }
    }

    private void assertJoinEquals(Join.JoinType type, Class<? extends Source> sourceClass, String sourceValue, String tableAlias, Join actual) {
        assertEquals(type, actual.getType());
        final var source = actual.getSource();
        assertFragmentEquals(sourceClass, sourceValue, tableAlias, source);
    }
}
