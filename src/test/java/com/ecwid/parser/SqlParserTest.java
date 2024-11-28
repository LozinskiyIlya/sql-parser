package com.ecwid.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SqlParserTest {

    private final SqlParser sqlParser = new SqlParser();

    @Test
    @DisplayName("Most basic select")
    void mostBasicSelect() {
        String sql = "SELECT * FROM table;";
        sqlParser.parse(sql);
    }

    @Test
    @DisplayName("Select with all commands")
    void selectWithAllCommands() {
        String sql = """
                SELECT author.name, count(book.id), sum(book.cost)
                FROM author
                         LEFT JOIN book ON (author.id = book.author_id)
                GROUP BY author.name
                HAVING COUNT(*) > 1
                   AND SUM(book.cost) > 500
                LIMIT 10;
                OFFSET 5;
                """;
        sqlParser.parse(sql);
    }

    @Test
    @DisplayName("One level nested source")
    void oneLevelNestedSource() {
        String sql = "select * from (select * from some_table) a_alias";
        sqlParser.parse(sql);
    }

    @Test
    @DisplayName("One level nested condition")
    void oneLevelNestedCondition() {
        String sql = """
                select *
                from users
                where id in (select user_id from participants where id = 'a')
                   or id = 2;
                """;
        sqlParser.parse(sql);
    }
}
