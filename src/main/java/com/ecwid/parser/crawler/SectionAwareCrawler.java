package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

abstract class SectionAwareCrawler implements Crawler {

    @Autowired
    private ApplicationContext applicationContext;

    public static final Map<String, Class<? extends Crawler>> QUERY_SECTIONS = new HashMap<>();

    static {
        QUERY_SECTIONS.put(LEX_SELECT, ColumnCrawler.class);
        QUERY_SECTIONS.put(LEX_FROM, SourceCrawler.class);
        QUERY_SECTIONS.put(LEX_JOIN, null);
        QUERY_SECTIONS.put(LEX_LEFT, null);
        QUERY_SECTIONS.put(LEX_RIGHT, null);
        QUERY_SECTIONS.put(LEX_FULL, null);
        QUERY_SECTIONS.put(LEX_WHERE, ClauseCrawler.class);
        QUERY_SECTIONS.put(LEX_HAVING, ClauseCrawler.class);
        QUERY_SECTIONS.put(LEX_AND, ClauseCrawler.class);
        QUERY_SECTIONS.put(LEX_OR, ClauseCrawler.class);
        QUERY_SECTIONS.put(LEX_GROUP, null);
        QUERY_SECTIONS.put(LEX_ORDER, null);
        QUERY_SECTIONS.put(LEX_LIMIT, LimitCrawler.class);
        QUERY_SECTIONS.put(LEX_OFFSET, OffsetCrawler.class);
        QUERY_SECTIONS.put(LEX_CLOSE_BRACKET, null);
    }

    public abstract void crawl(Query query, String currentSection, Supplier<String> fragmentSupplier);

    @Override
    public final void delegateToNextCrawler(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        Crawler.super.delegateToNextCrawler(query, currentSection, fragmentSupplier);
    }

    @Override
    public Crawler selectCrawler(String currentSection) {
        final var beanClass = QUERY_SECTIONS.get(currentSection);
        return beanClass == null ? null : applicationContext.getBean(beanClass);
    }
}
