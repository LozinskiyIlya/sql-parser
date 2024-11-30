package com.ecwid.parser;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.clause.ColumnOperand;
import com.ecwid.parser.fragment.clause.ConstantOperand;
import com.ecwid.parser.fragment.source.TableSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.ecwid.parser.fragment.clause.WhereClause.Operator.EQUALS;
import static org.junit.jupiter.api.Assertions.*;


@SpringJUnitConfig(SqlParser.class)
@DisplayName("Should parse query")
public class SqlParserIT {

    @Autowired
    SqlParser sqlParser;

    @Nested
    @DisplayName("When columns include")
    class Columns {
        @Test
        @DisplayName("only asterisk")
        void onlyAsterisk() throws Exception {
            String sql = "SELECT * FROM table;";
            Query parsed = sqlParser.parse(sql);
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
            String sql = "SELECT a, b, c FROM table;";
            Query parsed = sqlParser.parse(sql);
            assertEquals(3, parsed.getColumns().size());
            assertEquals("a", parsed.getColumns().get(0));
            assertEquals("b", parsed.getColumns().get(1));
            assertEquals("c", parsed.getColumns().get(2));
        }

        @Test
        @DisplayName("count and simple column name")
        void countAndSimpleName() throws Exception {
            String sql = "SELECT count(*), a FROM table;";
            Query parsed = sqlParser.parse(sql);
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
            String sql = "select * from (select * from some_table) a_alias";
            Query parsed = sqlParser.parse(sql);
            System.out.println(parsed);
        }
    }

    @Nested
    @DisplayName("When clause include")
    class Clause {
        @Test
        @DisplayName("simple where with constant")
        void simpleWhere() throws Exception {
            String sql = "SELECT * FROM table WHERE id = 1;";
            Query parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            final var clause = parsed.getWhereClauses().getFirst();
            assertEquals(EQUALS, clause.getOperator());
            final var left = clause.getLeftOperand();
            assertEquals(ColumnOperand.class, left.getClass());
            assertEquals("id", ((ColumnOperand) left).getColumn());
            final var right = clause.getRightOperand();
            assertEquals(ConstantOperand.class, right.getClass());
            assertEquals("1", ((ConstantOperand) right).getValue());
            System.out.println(parsed);
        }

        @Test
        @DisplayName("simple having")
        void simpleHaving() throws Exception {
            String sql = "SELECT * FROM table HAVING id = 1;";
            Query parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getWhereClauses().size());
            System.out.println(parsed);
        }

        @Test
        @DisplayName("one level nested condition and constant")
        void oneLevelNestedCondition() throws Exception {
            String sql = """
                    select *
                    from users
                    where id in (select user_id from participants where id = 'a')
                       or id = 2;
                    """;
            Query parsed = sqlParser.parse(sql);
            assertEquals(2, parsed.getWhereClauses().size());
            System.out.println(parsed);
        }
    }

    @Nested
    @DisplayName("When pagination includes")
    class Limit {
        @Test
        @DisplayName("limit")
        void limit() throws Exception {
            String sql = "SELECT * FROM table LIMIT 10;";
            Query parsed = sqlParser.parse(sql);
            assertEquals(10, parsed.getLimit());
        }

        @Test
        @DisplayName("offset")
        void offset() throws Exception {
            String sql = "SELECT * FROM table OFFSET 5;";
            Query parsed = sqlParser.parse(sql);
            assertEquals(5, parsed.getOffset());
        }

        @Test
        @DisplayName("limit and offset")
        void limitAndOffset() throws Exception {
            String sql = "SELECT * FROM table LIMIT 10 OFFSET 5;";
            Query parsed = sqlParser.parse(sql);
            assertEquals(10, parsed.getLimit());
            assertEquals(5, parsed.getOffset());
        }

        @Test
        @DisplayName("offset and limit")
        void offsetAndLimit() throws Exception {
            String sql = "SELECT * FROM table OFFSET 5 LIMIT 20;";
            Query parsed = sqlParser.parse(sql);
            assertEquals(20, parsed.getLimit());
            assertEquals(5, parsed.getOffset());
        }
    }

    @Test
    @DisplayName("with special chars in string")
    void specialCharsInString() throws Exception {
        String sql = "SELECT * FROM table WHERE id = 'special;chars(,)in.string';";
        Query parsed = sqlParser.parse(sql);
        System.out.println(parsed);
    }

    @Test
    @DisplayName("with all that beauty")
    void selectWithAllThatBeauty() throws Exception {
        String sql = """
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
        Query parsed = sqlParser.parse(sql);
        System.out.println(parsed);
    }
}
