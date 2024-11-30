package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;

import java.util.function.Supplier;

public interface Crawler {

    Crawler selectCrawler(String nextSection);

    void crawl(Query query, String currentSection, Supplier<String> fragmentSupplier);

    default void delegateToNextCrawler(Query query, String nextSection, Supplier<String> fragmentSupplier) {
        final var nextCrawler = selectCrawler(nextSection);
        if (nextCrawler != null) {
            nextCrawler.crawl(query, nextSection, fragmentSupplier);
        }
    }
}
