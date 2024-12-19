package com.ecwid.parser.crawler.base.helper;

import com.ecwid.parser.fragment.Query;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Getter
public class CrawlContext {
    private String currentLex;
    private final Query query;
    private final Supplier<String> nextLexSupplier;
    @Getter(AccessLevel.NONE)
    private final AtomicInteger openBrackets;

    public CrawlContext(Query query, String currentLex, Supplier<String> nextLexSupplier, int openBrackets) {
        this.query = query;
        this.currentLex = currentLex;
        this.nextLexSupplier = nextLexSupplier;
        this.openBrackets = new AtomicInteger(openBrackets);
    }

    public CrawlContext move() {
        return moveTo(nextLexSupplier.get());
    }

    public CrawlContext moveTo(String nextLex) {
        this.currentLex = nextLex;
        return this;
    }

    public int getOpenBrackets() {
        return openBrackets.get();
    }

    public int decrementOpenBrackets() {
        return openBrackets.decrementAndGet();
    }
}