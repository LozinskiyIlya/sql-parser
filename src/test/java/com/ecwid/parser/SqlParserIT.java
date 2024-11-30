package com.ecwid.parser;

import com.ecwid.parser.fragment.clause.*;
import com.ecwid.parser.fragment.source.QuerySource;
import com.ecwid.parser.fragment.source.TableSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.function.Function;

import static com.ecwid.parser.fragment.clause.WhereClause.ClauseType.*;
import static com.ecwid.parser.fragment.clause.WhereClause.Operator.EQUALS;
import static com.ecwid.parser.fragment.clause.WhereClause.Operator.IN;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringJUnitConfig(SqlParser.class)
@DisplayName("Should parse query")
public class SqlParserIT {
    protected final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    SqlParser sqlParser;

    @Nested
    @DisplayName("When columns include")
    class Columns {
        @Test
        @DisplayName("only asterisk")
        void onlyAsterisk() throws Exception {
            final var sql = "SELECT * FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getColumns().size());
            assertEquals("*", parsed.getColumns().getFirst());
            assertEquals(1, parsed.getFromSources().size());
            final var source = parsed.getFromSources().getFirst();
            assertEquals(TableSource.class, source.getClass());
            assertEquals("table", ((TableSource) source).getTableName());
        }

        @Test
        @DisplayName("multiple columns")
        void multipleColumns() throws Exception {
            final var sql = "SELECT a, b, c FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getColumns().size());
            assertEquals("a", parsed.getColumns().get(0));
            assertEquals("b", parsed.getColumns().get(1));
            assertEquals("c", parsed.getColumns().get(2));
        }

        @Test
        @DisplayName("count and simple column name")
        void countAndSimpleName() throws Exception {
            final var sql = "SELECT count(*), a FROM table;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getColumns().size());
            assertEquals("count(*)", parsed.getColumns().get(0));
            assertEquals("a", parsed.getColumns().get(1));
        }
    }

    @Nested
    @DisplayName("When sources include")
    class Sources {
        @Test
        @DisplayName("One level nested source")
        void oneLevelNestedSource() throws Exception {
            final var sql = "select * from (select * from some_table)";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getFromSources().size());
            final var source = parsed.getFromSources().getFirst();
            assertEquals(QuerySource.class, source.getClass());
            final var nestedQuery = ((QuerySource) source).getQuery();
            assertEquals(1, nestedQuery.getColumns().size());
            assertEquals("*", nestedQuery.getColumns().getFirst());
            assertEquals(1, nestedQuery.getFromSources().size());
            final var nestedSource = nestedQuery.getFromSources().getFirst();
            assertEquals(TableSource.class, nestedSource.getClass());
            assertEquals("some_table", ((TableSource) nestedSource).getTableName());
            System.out.println(parsed);
        }
    }

    @Nested
    @DisplayName("When clause include")
    class Clause {

        @Test
        @DisplayName("simple where with constant")
        void simpleWhere() throws Exception {
            final var sql = "SELECT * FROM table WHERE id = 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(WHERE, ColumnOperand.class, "id", EQUALS, ConstantOperand.class, "1", clause);
        }

        @Test
        @DisplayName("simple having")
        void simpleHaving() throws Exception {
            final var sql = "SELECT * FROM table HAVING id = 1;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertClauseEquals(HAVING, ColumnOperand.class, "id", EQUALS, ConstantOperand.class, "1", clause);
        }

        @Test
        @DisplayName("one level nested condition")
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
        }

        @Test
        @DisplayName("one level nested condition and constant")
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
        @DisplayName("constant and one level nested condition")
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

        private void assertClauseEquals(
                WhereClause.ClauseType type,
                Class<? extends Operand> leftType,
                Object leftVal,
                WhereClause.Operator operator,
                Class<? extends Operand> rightType,
                Object rightVal,
                WhereClause actual) {
            assertEquals(type, actual.getClauseType());
//          assertEquals(operator, clause.getOperator());

            final var leftOperand = actual.getLeftOperand();
            final var rightOperand = actual.getRightOperand();
            assertEquals(leftType, leftOperand.getClass());
            assertEquals(leftVal, getOperandValue().apply(leftOperand));
            assertEquals(rightType, rightOperand.getClass());
            assertEquals(rightVal, getOperandValue().apply(rightOperand));
        }


        private Function<Operand, Object> getOperandValue() {
            return operand -> switch (operand) {
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

    @Nested
    @DisplayName("When pagination includes")
    class Limit {
        @Test
        @DisplayName("limit")
        void limit() throws Exception {
            final var sql = "SELECT * FROM table LIMIT 10;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(10, parsed.getLimit());
        }

        @Test
        @DisplayName("offset")
        void offset() throws Exception {
            final var sql = "SELECT * FROM table OFFSET 5;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(5, parsed.getOffset());
        }

        @Test
        @DisplayName("limit and offset")
        void limitAndOffset() throws Exception {
            final var sql = "SELECT * FROM table LIMIT 10 OFFSET 5;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(10, parsed.getLimit());
            assertEquals(5, parsed.getOffset());
        }

        @Test
        @DisplayName("offset and limit")
        void offsetAndLimit() throws Exception {
            final var sql = "SELECT * FROM table OFFSET 5 LIMIT 20;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(20, parsed.getLimit());
            assertEquals(5, parsed.getOffset());
        }
    }

    @Test
    @DisplayName("with special chars in string")
    void specialCharsInString() throws Exception {
        final var sql = "SELECT * FROM table WHERE id = 'special;chars(,)in.string';";
        final var parsed = sqlParser.parse(sql);
        System.out.println(parsed);
    }

    @Test
    @DisplayName("with all that beauty")
    void selectWithAllThatBeauty() throws Exception {
        final var sql = """
                SELECT author.name, count(book.id), sum(book.cost) AS total_cost
                FROM author, (select * from old_books where title like '%$ecur1ty 1s our pri0r1ty%') as old_books
                LEFT JOIN book ON (author.id = book.author_id)
                GROUP BY author.name, author.id
                HAVING COUNT(*) >= 1
                   AND SUM(book.cost) > 500
                   OR book.id IN (SELECT id FROM book WHERE cost > 100)
                ORDER BY author.name DESC NULLS FIRST
                LIMIT 10
                OFFSET 5;
                """;
        final var parsed = sqlParser.parse(sql);
        System.out.println(parsed);
    }
}
