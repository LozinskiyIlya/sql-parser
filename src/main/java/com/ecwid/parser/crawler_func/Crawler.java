package com.ecwid.parser.crawler_func;

import com.ecwid.parser.fragment.enity.Query;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Crawler {

    Predicate<String> crawlUntil();

    BiConsumer<Query, String> addToQuery();

    Function<String, Crawler> next();

    default String crawl(Query query, Supplier<String> supplier) {
        return Stream.generate(supplier)
                .takeWhile(fr -> Objects.nonNull(fr) && crawlUntil().negate().test(fr))
                .peek(fragment -> addToQuery().accept(query, fragment))
                .reduce((fragment, last) -> last)
                .map(next())
                .map(next -> next.crawl(query, supplier))
                .orElse(null);
    }
}
