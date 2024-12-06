package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Map;
import java.util.function.Supplier;

abstract class SectionAwareCrawler implements Crawler {

    @Lazy
    @Autowired
    private Map<String, Crawler> sectionAgainstCrawler;

    public abstract void crawl(Query query, String currentSection, Supplier<String> fragments);

    protected final boolean shouldDelegate(String nextFragment) {
        return sectionAgainstCrawler.containsKey(nextFragment);
    }

    @Override
    public final void delegate(Query query, String currentSection, Supplier<String> fragments) {
        Crawler.super.delegate(query, currentSection, fragments);
    }

    @Override
    public final Crawler nextCrawler(String currentSection) {
        return sectionAgainstCrawler.get(currentSection);

    }
}
