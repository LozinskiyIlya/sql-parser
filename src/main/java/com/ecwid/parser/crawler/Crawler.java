package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;

import java.util.function.Supplier;

public interface Crawler {

    Crawler selectNext(String currentSection);

    void crawl(Query query, String currentSection, Supplier<String> fragmentSupplier);

    default void delegateToNext(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        final var nextCrawler = selectNext(currentSection);
        if (nextCrawler != null) {
            nextCrawler.crawl(query, currentSection, fragmentSupplier);
        }
    }
}
