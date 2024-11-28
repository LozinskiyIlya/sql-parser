package com.ecwid.parser;

import com.ecwid.parser.fragments.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SqlParserTest {

    private final SqlParser sqlParser = new SqlParser();

    @Test
    @DisplayName("Most basic select")
    void mostBasicSelect() throws Exception {
        String sql = "SELECT * FROM table;";
        Query parsed = sqlParser.parse(sql);
        System.out.println(parsed);
    }

    @Test
    @DisplayName("Special chars in string")
    void specialCharsInString() throws Exception {
        String sql = "SELECT * FROM table WHERE id = 'special;chars(,)in.string';";
        Query parsed = sqlParser.parse(sql);
        System.out.println(parsed);
    }

    @Test
    @DisplayName("Select with all that beauty")
    void selectWithAllThatBeauty() throws Exception {
        String sql = """
                SELECT author.name, count(book.id), sum(book.cost) AS total_cost
                FROM author, (select * from old_books where title like '%$ecur1ty 1s our pri0r1ty%') as old_books
                LEFT JOIN book ON (author.id = book.author_id)
                GROUP BY author.name, author.id
                HAVING COUNT(*) > 1
                   AND SUM(book.cost) > 500
                   OR book.id IN (SELECT id FROM book WHERE cost > 100)
                ORDER BY author.name DESC NULLS FIRST
                LIMIT 10
                OFFSET 5;
                """;
        Query parsed = sqlParser.parse(sql);
        System.out.println(parsed);
    }

    @Test
    @DisplayName("One level nested source")
    void oneLevelNestedSource() throws Exception {
        String sql = "select * from (select * from some_table) a_alias";
        Query parsed = sqlParser.parse(sql);
        System.out.println(parsed);
    }

    @Test
    @DisplayName("One level nested condition")
    void oneLevelNestedCondition() throws Exception {
        String sql = """
                select *
                from users
                where id in (select user_id from participants where id = 'a')
                   or id = 2;
                """;
        Query parsed = sqlParser.parse(sql);
        System.out.println(parsed);
    }
}
