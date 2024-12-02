package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

abstract class SectionAwareCrawler implements Crawler {

    @Autowired
    private ApplicationContext applicationContext;

    private static final Map<String, Class<? extends Crawler>> SECTION_AGAINST_CRAWLER = new HashMap<>();
    private static final Map<String, Class<? extends Crawler>> SUB_SECTION_AGAINST_CRAWLER = new HashMap<>();

    static {
        SECTION_AGAINST_CRAWLER.put(LEX_SELECT, ColumnCrawler.class);
        SECTION_AGAINST_CRAWLER.put(LEX_FROM, SourceCrawler.class);
        SECTION_AGAINST_CRAWLER.put(LEX_JOIN, null);
        SECTION_AGAINST_CRAWLER.put(LEX_WHERE, ClauseCrawler.class);
        SECTION_AGAINST_CRAWLER.put(LEX_HAVING, ClauseCrawler.class);
        SECTION_AGAINST_CRAWLER.put(LEX_GROUP, null);
        SECTION_AGAINST_CRAWLER.put(LEX_ORDER, null);
        SECTION_AGAINST_CRAWLER.put(LEX_LIMIT, LimitCrawler.class);
        SECTION_AGAINST_CRAWLER.put(LEX_OFFSET, OffsetCrawler.class);

        SUB_SECTION_AGAINST_CRAWLER.put(LEX_AND, ClauseCrawler.class);
        SUB_SECTION_AGAINST_CRAWLER.put(LEX_OR, ClauseCrawler.class);
        SUB_SECTION_AGAINST_CRAWLER.put(LEX_LEFT, null);
        SUB_SECTION_AGAINST_CRAWLER.put(LEX_RIGHT, null);
        SUB_SECTION_AGAINST_CRAWLER.put(LEX_FULL, null);
        SUB_SECTION_AGAINST_CRAWLER.put(LEX_CLOSE_BRACKET, NoopCrawler.class);
    }

    public abstract void crawl(Query query, String currentSection, Supplier<String> nextFragmentSupplier);

    protected final boolean shouldDelegate(String nextFragment) {
        return SECTION_AGAINST_CRAWLER.containsKey(nextFragment) || SUB_SECTION_AGAINST_CRAWLER.containsKey(nextFragment);
    }

    @Override
    public final void delegate(Query query, String currentSection, Supplier<String> nextFragmentSupplier) {
        Crawler.super.delegate(query, currentSection, nextFragmentSupplier);
    }

    @Override
    public Crawler nextCrawler(String currentSection) {
        final var beanClass = SECTION_AGAINST_CRAWLER.getOrDefault(currentSection, SUB_SECTION_AGAINST_CRAWLER.get(currentSection));
        return beanClass == null ? null : applicationContext.getBean(beanClass);
    }
}
