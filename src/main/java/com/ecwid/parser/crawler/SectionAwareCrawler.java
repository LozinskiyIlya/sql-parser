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
        QUERY_SECTIONS.put(LEX_WHERE, null);
        QUERY_SECTIONS.put(LEX_HAVING, null);
        QUERY_SECTIONS.put(LEX_GROUP, null);
        QUERY_SECTIONS.put(LEX_ORDER, null);
        QUERY_SECTIONS.put(LEX_LIMIT, LimitCrawler.class);
        QUERY_SECTIONS.put(LEX_OFFSET, OffsetCrawler.class);
    }


    abstract void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier);

    public final void crawl(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        checkSection(currentSection);
        addQueryFragment(query, currentSection, fragmentSupplier);
    }

    @Override
    public final void delegateToNext(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        Crawler.super.delegateToNext(query, currentSection, fragmentSupplier);
    }

    @Override
    public Crawler selectNext(String currentSection) {
        final var beanClass = QUERY_SECTIONS.get(currentSection);
        if (beanClass == null) {
            return null;
        }
        return applicationContext.getBean(beanClass);
    }

    private void checkSection(String currentSection) {
//        if (!isOptional() && !section.equals(currentSection)) {
//            throw new IllegalStateException("Unexpected token ^" + currentSection);
//        }
    }
}
