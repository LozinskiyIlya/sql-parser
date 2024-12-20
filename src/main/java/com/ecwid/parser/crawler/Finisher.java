package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.Crawler;
import com.ecwid.parser.crawler.base.helper.CrawlContext;

import java.util.Optional;

import static com.ecwid.parser.Lexemes.LEX_EMPTY;
import static com.ecwid.parser.Lexemes.LEX_SEMICOLON;

@LexemeHandler(lexemes = {LEX_SEMICOLON, LEX_EMPTY})
public class Finisher implements Crawler {
    @Override
    public Optional<Crawler> nextCrawler(String nextSection) {
        return Optional.empty();
    }

    @Override
    public void crawl(CrawlContext context) {
        if (context.getOpenBrackets() != 0) {
            throw new IllegalStateException("Not balanced brackets in query: " + context.getQuery());
        }
    }
}
