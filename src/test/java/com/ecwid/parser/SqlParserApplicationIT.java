package com.ecwid.parser;

import com.ecwid.parser.fragment.enity.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("Should parse query")
public class SqlParserApplicationIT extends AbstractSpringParserTest {

    @Test
    @DisplayName("and stop on semi-colon")
    void selectStopsOnSemiColon() throws Exception {
        final var sql = "SELECT * FROM table; , another_table;";
        final var parsed = sqlParser.parse(sql);
        assertEquals(1, parsed.getSources().size());
        final var onlySource = (Table) parsed.getSources().getFirst();
        assertEquals("table", onlySource.name());
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
