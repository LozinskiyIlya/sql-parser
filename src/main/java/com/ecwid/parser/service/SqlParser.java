package com.ecwid.parser.service;


import com.ecwid.parser.crawler.ColumnCrawler;
import com.ecwid.parser.crawler.base.helper.CrawlContext;
import com.ecwid.parser.fragment.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;

import static com.ecwid.parser.Lexemes.LEX_SELECT;

@Service
@RequiredArgsConstructor
public class SqlParser {

    private final ColumnCrawler firstCrawler;
    private final LexemeReader lexemeReader;

    public Query parse(String sql) throws IOException {
        final var reader = readerFromString(sql);
        try {
            return parse(reader);
        } catch (IOException e) {
            reader.close();
            throw e;
        }
    }

    public Query parse(PushbackReader reader) throws IOException {
        return parseQuery(reader);
    }

    private Query parseQuery(PushbackReader reader) throws IllegalStateException {
        final var section = lexemeReader.nextLex(reader);
        if (!LEX_SELECT.equals(section)) {
            throw new IllegalStateException("Query should start with SELECT keyword");
        }
        final var query = new Query();
        firstCrawler.crawl(new CrawlContext(query, section, () -> lexemeReader.nextLex(reader), 0));
        return query;
    }

    public static PushbackReader readerFromString(String s) {
        return new PushbackReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
    }

}
