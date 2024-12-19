package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.Crawler;
import com.ecwid.parser.fragment.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Slf4j
@LexemeHandler(lexemes = {LEX_SEMICOLON, LEX_EMPTY})
public class Finisher implements Crawler {
    @Override
    public Optional<Crawler> nextCrawler(String nextSection) {
        return Optional.empty();
    }

    @Override
    public void crawl(Query query, String currentSection, Supplier<String> nextLex, int openBrackets) {
        if (openBrackets != 0) {
            throw new IllegalStateException("Not balanced brackets in query: " + query);
        }
        log.info("Query {} finished with '{}'", query, currentSection);
    }
}
