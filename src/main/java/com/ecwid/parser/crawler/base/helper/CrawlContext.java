package com.ecwid.parser.crawler.base.helper;

import com.ecwid.parser.fragment.Query;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Getter
public class CrawlContext {
    private String current;
    private final Query query;
    private final Supplier<String> next;
    @Getter(AccessLevel.NONE)
    private final AtomicInteger openBrackets;

    public CrawlContext(Query query, String current, Supplier<String> next, int openBrackets) {
        this.current = current;
        this.query = query;
        this.next = next;
        this.openBrackets = new AtomicInteger(openBrackets);
    }

    public void move() {
        moveTo(next.get());
    }

    public CrawlContext moveTo(String nextLex) {
        this.current = nextLex;
        return this;
    }

    public int getOpenBrackets() {
        return openBrackets.get();
    }

    public int decrementOpenBrackets() {
        return openBrackets.decrementAndGet();
    }
}