package com.ecwid.parser.crawler_func;

import com.ecwid.parser.fragment.enity.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

public abstract class SectionAwareCrawler implements Crawler {

    @Autowired
    private ApplicationContext applicationContext;

    private static final Map<String, Class<? extends Crawler>> SECTION_AGAINST_CRAWLER = new HashMap<>();
    private static final Map<String, Class<? extends Crawler>> SUB_SECTION_AGAINST_CRAWLER = new HashMap<>();

    static {
        SECTION_AGAINST_CRAWLER.put(LEX_SELECT, ColumnCrawler.class);
        SECTION_AGAINST_CRAWLER.put(LEX_FROM, null);
        SECTION_AGAINST_CRAWLER.put(LEX_JOIN, null);
        SECTION_AGAINST_CRAWLER.put(LEX_WHERE, null);
        SECTION_AGAINST_CRAWLER.put(LEX_HAVING, null);
        SECTION_AGAINST_CRAWLER.put(LEX_GROUP, null);
        SECTION_AGAINST_CRAWLER.put(LEX_ORDER, null);
        SECTION_AGAINST_CRAWLER.put(LEX_LIMIT, null);
        SECTION_AGAINST_CRAWLER.put(LEX_OFFSET, null);

        SUB_SECTION_AGAINST_CRAWLER.put(LEX_AND, null);
        SUB_SECTION_AGAINST_CRAWLER.put(LEX_OR, null);
        SUB_SECTION_AGAINST_CRAWLER.put(LEX_LEFT, null);
        SUB_SECTION_AGAINST_CRAWLER.put(LEX_RIGHT, null);
        SUB_SECTION_AGAINST_CRAWLER.put(LEX_FULL, null);
    }

    protected Predicate<String> crawlUntil;
    protected BiConsumer<Query, String> addToQuery;

    @Override
    public final Predicate<String> crawlUntil() {
        return shouldDelegate.or(crawlUntil);
    }

    @Override
    public final BiConsumer<Query, String> addToQuery() {
        return addToQuery;
    }

    @Override
    public final Function<String, Crawler> next() {
        return fragment -> Optional.ofNullable(SECTION_AGAINST_CRAWLER.getOrDefault(fragment,
                        SUB_SECTION_AGAINST_CRAWLER.get(fragment)))
                .map(applicationContext::getBean)
                .orElse(null);
    }

    @Override
    public final String crawl(Query query, Supplier<String> supplier) {
        return Crawler.super.crawl(query, supplier);
    }

    private final Predicate<String> shouldDelegate = ((Predicate<String>) SECTION_AGAINST_CRAWLER::containsKey)
            .or(SUB_SECTION_AGAINST_CRAWLER::containsKey);
}
