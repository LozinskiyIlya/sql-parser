package com.ecwid.parser;

import com.ecwid.parser.crawler.ColumnCrawler;
import com.ecwid.parser.fragment.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.ecwid.parser.Lexemes.*;

@Configuration
@ComponentScan(basePackages = "com.ecwid.parser")
@RequiredArgsConstructor
public class SqlParser {

    private final ColumnCrawler columnCrawler;

    public static void main(String[] args) {
        final var context = new AnnotationConfigApplicationContext(SqlParser.class);
        final var sqlParser = context.getBean(SqlParser.class);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Type next query");
            final var query = sqlParser.parse(reader);
            System.out.println(query);
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    public Query parse(String sql) throws IOException {
        return parse(readerFromString(sql));
    }

    public Query parse(BufferedReader reader) throws IOException {
        System.out.println("Parsing query...");
        final var query = parseQuery(reader);
        System.out.println("Query parsed");
        return query;
    }

    private Query parseQuery(BufferedReader reader) throws IllegalStateException {
        final var command = nextLex(reader);
        final var query = new Query();
        columnCrawler.crawl(query, command, () -> nextLex(reader));
        return query;
    }

    private static void skipJoinKeywordIfNeeded(String currentLex, BufferedReader reader) {
        if (JOIN_TYPES.contains(currentLex)) {
            final var shouldBeJoin = nextLex(reader);
            if (!LEX_JOIN.equals(shouldBeJoin)) {
                throw new IllegalStateException("JOIN keyword expected");
            }
        }
    }

    private static String nextLex(BufferedReader reader) {
        int character;
        final var lex = new StringBuilder();
        try {
            while ((character = reader.read()) != -1) {
                final var currentChar = (char) character;
                final var currentCharAsString = String.valueOf(currentChar);
                if (LEX_SINGLE_QUOTE.equals(currentCharAsString)) {
                    return readStringToTheEnd(reader);
                }
                if (Character.isWhitespace(currentChar) || SEPARATORS.contains(currentCharAsString)) {
                    if (lex.isEmpty()) {
                        continue;
                    }
                    return lex.toString().toLowerCase();
                }
                lex.append(currentChar);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lex.isEmpty() ? null : lex.toString().toLowerCase();
    }

    private static String readStringToTheEnd(BufferedReader reader) throws IOException, IllegalStateException {
        int character;
        final var lex = new StringBuilder(LEX_SINGLE_QUOTE);
        while ((character = reader.read()) != -1) {
            char c = (char) character;
            lex.append(c);
            if (LEX_SINGLE_QUOTE.equals(String.valueOf(c))) {
                return lex.toString();
            }
        }
        throw new IllegalStateException("String is not closed");
    }

    private static BufferedReader readerFromString(String s) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
    }
}
