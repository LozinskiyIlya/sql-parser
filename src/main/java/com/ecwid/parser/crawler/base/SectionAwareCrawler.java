package com.ecwid.parser.crawler.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    protected boolean shouldDelegate(String nextSection) {
        return sectionAgainstCrawler.containsKey(nextSection);
    }


    protected final String crawlUntilAndReturnNext(Predicate<String> lexIs, Consumer<String> andDoAction, Supplier<String> nextLex) {
        String lex;
        while ((lex = nextLex.get()) != null) {
            if (lexIs.test(lex)) {
                break;
            }
            andDoAction.accept(lex);
        }
        return lex;
    }
}
