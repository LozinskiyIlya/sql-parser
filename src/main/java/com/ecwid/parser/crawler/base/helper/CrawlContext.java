package com.ecwid.parser.crawler.base.helper;

import com.ecwid.parser.fragment.Query;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Getter
public class CrawlContext {
    private String currentSection;
    private final Query query;
    private final Supplier<String> nextLex;
    @Getter(AccessLevel.NONE)
    private final AtomicInteger openBrackets;

    public CrawlContext(Query query, String currentSection, Supplier<String> nextLex, int openBrackets) {
        this.query = query;
        this.currentSection = currentSection;
        this.nextLex = nextLex;
        this.openBrackets = new AtomicInteger(openBrackets);
    }

    public CrawlContext moveTo(String nextSection) {
        this.currentSection = nextSection;
        return this;
    }

    public int getOpenBrackets() {
        return openBrackets.get();
    }

    public int decrementOpenBrackets() {
        return openBrackets.decrementAndGet();
    }
}