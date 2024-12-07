package com.ecwid.parser;

import com.ecwid.parser.fragment.Join;
import com.ecwid.parser.fragment.Condition.ClauseType;
import com.ecwid.parser.fragment.Condition.Operator;
import com.ecwid.parser.fragment.ConstantListOperand;
import com.ecwid.parser.fragment.ConstantOperand;
import com.ecwid.parser.fragment.Column;
import com.ecwid.parser.fragment.Query;
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
            assertJoinTableEquals(JoinType.JOIN, "table2", null, join);
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
            assertJoinTableEquals(JoinType.JOIN, "table2", "t", join);
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
            assertJoinTableEquals(JoinType.JOIN, "table2", "t", join);
            final var condition = join.getConditions().getFirst();
            assertConditionEquals(ClauseType.ON, Column.class, "table1.id", Operator.EQUALS, Column.class, "t.id", condition);
        }

        @Test
        @DisplayName("multiple conditions")
        void joinWithMultipleConditions() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 ON table1.id1 = table2.id1 AND table1.id2 >= 2;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinTableEquals(JoinType.JOIN, "table2", null, join);
            final var conditions = join.getConditions();
            assertEquals(2, conditions.size());
            assertConditionEquals(ClauseType.ON, Column.class, "table1.id1", Operator.EQUALS, Column.class, "table2.id1", conditions.get(0));
            assertConditionEquals(ClauseType.AND, Column.class, "table1.id2", Operator.GREATER_THAN_OR_EQUALS, ConstantOperand.class, "2", conditions.get(1));
        }

        @Test
        @DisplayName("function as first operand")
        void joinWithFunctionAsFirstOperand() throws Exception {
            final var sql = "SELECT * FROM table1 JOIN table2 t ON count(table1.*) = t.some_counter;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinTableEquals(JoinType.JOIN, "table2", "t", join);
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
            assertJoinTableEquals(JoinType.NATURAL_FULL_OUTER, "table2", null, join);
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
            assertJoinTableEquals(JoinType.JOIN, "b", null, join);
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
            assertJoinTableEquals(JoinType.JOIN, "table_b", "b", join);
            final var conditions = join.getConditions();
            assertEquals(1, conditions.size());
            assertConditionEquals(ClauseType.ON, Column.class, "a.id", Operator.IN, Query.class, nested, conditions.getFirst());

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
                assertJoinTableEquals(JoinType.JOIN, "b", null, joins.get(0));
                assertJoinTableEquals(JoinType.JOIN, "c", "d", joins.get(1));
                assertJoinTableEquals(JoinType.JOIN, "e", "f", joins.get(2));
                assertJoinTableEquals(JoinType.JOIN, "g", "h", joins.get(3));
                assertJoinTableEquals(JoinType.JOIN, "i", "j", joins.get(4));
                assertJoinTableEquals(JoinType.JOIN, "k", null, joins.get(5));
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
                assertJoinTableEquals(JoinType.NATURAL_FULL_OUTER, "table2", null, firstJoin);
                assertConditionEquals(ClauseType.ON, Column.class, "table1.id", Operator.EQUALS, Column.class, "table2.id", firstJoin.getConditions().getFirst());
                final var secondJoin = parsed.getJoins().getLast();
                assertJoinTableEquals(JoinType.LEFT, "table3", null, secondJoin);
                assertConditionEquals(ClauseType.ON, Column.class, "table2.id", Operator.EQUALS, Column.class, "table3.id", secondJoin.getConditions().getFirst());
            }


            @Test
            @DisplayName("different conditions")
            void differentConditions() throws Exception {
                final var sql = """
                        SELECT *
                        FROM a
                        JOIN b ON a.id = b.id
                        JOIN c ON b.id = c.id OR a.id IN (1, 2)
                        JOIN d ON a.id LIKE b.id AND d.id IS NOT NULL;
                        """;
                final var parsed = sqlParser.parse(sql);
                assertEquals(3, parsed.getJoins().size());
                final var joins = parsed.getJoins();
                assertJoinTableEquals(JoinType.JOIN, "b", null, joins.get(0));
                assertJoinTableEquals(JoinType.JOIN, "c", null, joins.get(1));
                assertJoinTableEquals(JoinType.JOIN, "d", null, joins.get(2));
                final var conditions = joins.get(0).getConditions();
                assertEquals(1, conditions.size());
                assertConditionEquals(ClauseType.ON, Column.class, "a.id", Operator.EQUALS, Column.class, "b.id", conditions.getFirst());
                final var conditions1 = joins.get(1).getConditions();
                assertEquals(2, conditions1.size());
                assertConditionEquals(ClauseType.ON, Column.class, "b.id", Operator.EQUALS, Column.class, "c.id", conditions1.get(0));
                assertConditionEquals(ClauseType.OR, Column.class, "a.id", Operator.IN, ConstantListOperand.class, List.of("1", "2"), conditions1.get(1));
                final var conditions2 = joins.get(2).getConditions();
                assertEquals(2, conditions2.size());
                assertConditionEquals(ClauseType.ON, Column.class, "a.id", Operator.LIKE, Column.class, "b.id", conditions2.get(0));
                assertConditionEquals(ClauseType.AND, Column.class, "d.id", Operator.IS_NOT, ConstantOperand.class, "null", conditions2.get(1));
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
                assertJoinTableEquals(JoinType.JOIN, "table_b", "b", joins.get(0));
                assertJoinTableEquals(JoinType.JOIN, "table_c", "c", joins.get(1));
                final var conditions = joins.get(0).getConditions();
                assertEquals(2, conditions.size());
                assertConditionEquals(ClauseType.ON, Column.class, "a.id", Operator.EQUALS, Column.class, "b.a_id", conditions.get(0));
                assertConditionEquals(ClauseType.AND, Column.class, "b.date", Operator.GREATER_THAN, ConstantOperand.class, "'2023-01-01'", conditions.get(1));
                final var conditions1 = joins.get(1).getConditions();
                assertEquals(3, conditions1.size());
                assertConditionEquals(ClauseType.ON, Column.class, "b.id", Operator.EQUALS, Column.class, "c.b_id", conditions1.get(0));
                assertConditionEquals(ClauseType.AND, Column.class, "c.status", Operator.EQUALS, ConstantOperand.class, "'pending'", conditions1.get(1));
                assertConditionEquals(ClauseType.AND, Column.class, "c.id", Operator.IN, Query.class, nested, conditions1.get(2));
            }
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
                    assertJoinTableEquals(joinType, "table1", null, join);
                });
            });
        }

        private void assertJoinTableEquals(Join.JoinType type, String tableName, String tableAlias, Join join) {
            assertEquals(type, join.getType());
            final var table = join.getTable();
//            assertEquals(tableName, table.name());
            assertEquals(tableAlias, table.getAlias());
        }
    }
}
