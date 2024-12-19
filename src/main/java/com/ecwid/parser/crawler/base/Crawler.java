package com.ecwid.parser.crawler.base;

import com.ecwid.parser.crawler.base.helper.CrawlContext;

import java.util.Optional;

public interface Crawler {

    Optional<Crawler> nextCrawler(String nextSection);

    void crawl(CrawlContext context);

    default void delegate(CrawlContext context) {
        nextCrawler(context.getCurrentSection()).ifPresent(crawler -> crawler.crawl(context));
    }


}
