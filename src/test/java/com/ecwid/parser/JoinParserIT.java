package com.ecwid.parser;

import com.ecwid.parser.fragment.Join;
import com.ecwid.parser.fragment.condition.Condition.ClauseType;
import com.ecwid.parser.fragment.condition.Condition.Operator;
import com.ecwid.parser.fragment.condition.ConstantListOperand;
import com.ecwid.parser.fragment.condition.ConstantOperand;
import com.ecwid.parser.fragment.domain.Column;
import org.junit.jupiter.api.*;

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
        @DisplayName("complex join type")
        void joinWithComplexType() throws Exception {
            final var sql = "SELECT * FROM table1 NATURAL FULL OUTER JOIN table2 ON table1.id = table2.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
            assertJoinTableEquals(JoinType.NATURAL_FULL_OUTER, "table2", null, join);
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
        assertEquals(tableName, table.name());
        assertEquals(tableAlias, table.alias());
    }
}
