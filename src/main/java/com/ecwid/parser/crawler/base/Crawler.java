package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.Query;

import java.util.Optional;
import java.util.function.Supplier;

public interface Crawler {

    Optional<Crawler> nextCrawler(String nextSection);

    void crawl(CrawlContext context);

    default void delegate(CrawlContext context) {
        nextCrawler(context.currentSection()).ifPresent(crawler -> crawler.crawl(context));
    }

    record CrawlContext(Query query, String currentSection, Supplier<String> nextLex, int openBrackets) {
    }
}
