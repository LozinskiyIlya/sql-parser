package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.Crawler;
import com.ecwid.parser.fragment.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Component
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
