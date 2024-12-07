package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Crawler {

    Optional<Crawler> nextCrawler(String nextSection);

    void crawl(Query query, String currentSection, Supplier<String> fragments);

    default void delegate(Query query, String nextSection, Supplier<String> fragments) {
        nextCrawler(nextSection).ifPresent(crawler -> crawler.crawl(query, nextSection, fragments));
    }

    default String crawlUntilAndReturnNext(Predicate<String> fragmentIs, Consumer<String> andDoAction, Supplier<String> fragments) {
        String fragment;
        while ((fragment = fragments.get()) != null) {
            if (fragmentIs.test(fragment)) {
                break;
            }
            andDoAction.accept(fragment);
        }
        return fragment;
    }
}
