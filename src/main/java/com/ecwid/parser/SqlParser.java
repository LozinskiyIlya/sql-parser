package com.ecwid.parser;

import com.ecwid.parser.crawler.ColumnCrawler;
import com.ecwid.parser.fragment.enity.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ecwid.parser.Lexemes.*;

@Configuration
@ComponentScan(basePackages = "com.ecwid.parser")
@RequiredArgsConstructor
public class SqlParser {

    private static final Logger logger = Logger.getLogger(SqlParser.class.getName());

    static {
        logger.setLevel(Level.INFO);
    }

    private final ColumnCrawler columnCrawler;

    public static void main(String[] args) {
        final var context = new AnnotationConfigApplicationContext(SqlParser.class);
        final var sqlParser = context.getBean(SqlParser.class);
        try (PushbackReader reader = new PushbackReader(new InputStreamReader(System.in))) {
            logger.log(Level.INFO, "Enter SQL query:");
            final var query = sqlParser.parse(reader);
            logger.log(Level.INFO, query.toString());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while reading SQL query", e);
        }
    }

    public Query parse(String sql) throws IOException {
        return parse(readerFromString(sql));
    }

    public Query parse(PushbackReader reader) throws IOException {
        return parseQuery(reader);
    }

    private Query parseQuery(PushbackReader reader) throws IllegalStateException {
        final var section = nextLex(reader);
        if (!LEX_SELECT.equals(section)) {
            throw new IllegalStateException("Query should start with SELECT keyword");
        }
        final var query = new Query();
        columnCrawler.crawl(query, section, () -> nextLex(reader));
        return query;
    }

    private static void skipJoinKeywordIfNeeded(String currentLex, PushbackReader reader) {
        if (JOIN_TYPES.contains(currentLex)) {
            final var shouldBeJoin = nextLex(reader);
            if (!LEX_JOIN.equals(shouldBeJoin)) {
                throw new IllegalStateException("JOIN keyword expected");
            }
        }
    }

    private static String nextLex(PushbackReader reader) {
        int character;
        final var lex = new StringBuilder();
        try {
            while ((character = reader.read()) != -1) {
                final var currentChar = (char) character;
                final var currentCharAsString = String.valueOf(currentChar);
                if (LEX_SINGLE_QUOTE.equals(currentCharAsString)) {
                    return readStringToTheEnd(reader);
                }
                if (BRACKETS.contains(currentCharAsString)) {
                    if (lex.isEmpty()) {
                        return currentCharAsString;
                    }
                    reader.unread(currentChar);
                    return lex.toString().toLowerCase();
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

    private static String readStringToTheEnd(PushbackReader reader) throws IOException, IllegalStateException {
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

    private static PushbackReader readerFromString(String s) {
        return new PushbackReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
    }
}
