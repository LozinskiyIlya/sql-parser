package com.ecwid.parser.crawler.base;

import com.ecwid.parser.crawler.base.helper.CrawlContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Map;
import java.util.Optional;

public abstract class SectionAwareCrawler implements Crawler {

    @Lazy
    @Autowired
    private Map<String, Crawler> sectionAgainstCrawler;

    @Override
    public final void delegate(CrawlContext context) {
        Crawler.super.delegate(context);
    }

    @Override
    public Optional<Crawler> nextCrawler(String currentSection) {
        return Optional.ofNullable(sectionAgainstCrawler.get(currentSection));
    }

    protected boolean shouldDelegate(String nextSection) {
        return sectionAgainstCrawler.containsKey(nextSection);
    }
}
