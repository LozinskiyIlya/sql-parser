package com.ecwid.parser.service;


import com.ecwid.parser.crawler_old.ColumnCrawler;
import com.ecwid.parser.fragment.enity.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;

import static com.ecwid.parser.Lexemes.*;

@Service
@RequiredArgsConstructor
public class SqlParser {

    private final ColumnCrawler firstCrawler;
    private final LexemeReader lexemeReader;

    public Query parse(String sql) throws IOException {
        return parse(readerFromString(sql));
    }

    public Query parse(PushbackReader reader) throws IOException {
        try (reader) {
            return parseQuery(reader);
        } catch (IllegalStateException e) {
            throw new IOException(e);
        }
    }

    private Query parseQuery(PushbackReader reader) throws IllegalStateException {
        final var section = lexemeReader.nextLex(reader);
        if (!LEX_SELECT.equals(section)) {
            throw new IllegalStateException("Query should start with SELECT keyword");
        }
        final var query = new Query();
        firstCrawler.crawl(query, section, () -> {
            final var nextLex = lexemeReader.nextLex(reader);
            return LEX_SEMICOLON.equals(nextLex) ? null : nextLex;
        });
        return query;
    }

    private void skipJoinKeywordIfNeeded(String currentLex, PushbackReader reader) {
        if (JOIN_TYPES.contains(currentLex)) {
            final var shouldBeJoin = lexemeReader.nextLex(reader);
            if (!LEX_JOIN.equals(shouldBeJoin)) {
                throw new IllegalStateException("JOIN keyword expected");
            }
        }
    }

    public static PushbackReader readerFromString(String s) {
        return new PushbackReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
    }

}
