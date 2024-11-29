package com.ecwid.parser;

import com.ecwid.parser.crawler.ColumnCrawler;
import com.ecwid.parser.crawler.SourceCrawler;
import com.ecwid.parser.fragment.Query;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.ecwid.parser.Lexemes.*;


public class SqlParser {


    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Type next query");
            final var sqlParser = new SqlParser();
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

    private static Query parseQuery(BufferedReader reader) throws IllegalStateException {
        final var command = nextLex(reader);
        final var query = new Query();
        final var crawler = new ColumnCrawler(new SourceCrawler(null));
        crawler.addQueryFragment(query, command, () -> nextLex(reader));
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
