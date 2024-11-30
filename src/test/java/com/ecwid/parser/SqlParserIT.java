package com.ecwid.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Should parse query")
public class SqlParserIT extends AbstractSpringParserTest {

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
