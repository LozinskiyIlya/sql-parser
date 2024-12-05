package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.Query;

import java.util.function.Supplier;

public interface Crawler {

    Crawler nextCrawler(String nextSection);

    void crawl(Query query, String currentSection, Supplier<String> fragments);

    default void delegate(Query query, String nextSection, Supplier<String> fragments) {
        final var nextCrawler = nextCrawler(nextSection);
        if (nextCrawler != null) {
            nextCrawler.crawl(query, nextSection, fragments);
        }
    }
}
