package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.Query;

import java.util.Optional;
import java.util.function.Supplier;

public interface Crawler {

    Optional<Crawler> nextCrawler(String nextSection);

    void crawl(Query query, String currentSection, Supplier<String> nextLex);

    default void delegate(Query query, String nextSection, Supplier<String> nextLex) {
        nextCrawler(nextSection).ifPresent(crawler -> crawler.crawl(query, nextSection, nextLex));
    }
}
