package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Query;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Crawler {

    Crawler nextCrawler(String nextSection);

    void crawl(Query query, String currentSection, Supplier<String> nextFragmentSupplier);

    default void delegate(Query query, String nextSection, Supplier<String> nextFragmentSupplier) {
        final var nextCrawler = nextCrawler(nextSection);
        if (nextCrawler != null) {
            nextCrawler.crawl(query, nextSection, nextFragmentSupplier);
        }
    }

    default String crawlUntilAndReturnNext(Predicate<String> fragmentIs, Consumer<String> andDoAction, Supplier<String> nextFragmentSupplier) {
        String fragment;
        while ((fragment = nextFragmentSupplier.get()) != null && fragmentIs.negate().test(fragment)) {
            andDoAction.accept(fragment);
        }
        return fragment;
    }

    default String crawlListAndReturnNext(Consumer<String> onItem, Supplier<String> nextFragmentSupplier) {
        return crawlUntilAndReturnNext(fragment -> false, onItem, nextFragmentSupplier);
    }
}
