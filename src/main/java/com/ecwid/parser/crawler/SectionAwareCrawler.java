package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;

import java.util.function.Supplier;


abstract class SectionAwareCrawler implements Crawler {
    private final Crawler next;
    private final String section;

    SectionAwareCrawler(final Crawler next, final String section) {
        this.next = next;
        this.section = section;
    }
    abstract boolean isOptional();

    abstract void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier);

    public final void addFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        checkSection(currentSection);
        addQueryFragment(query, currentSection, fragmentSupplier);
    }

    @Override
    public final void delegateToNext(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        Crawler.super.delegateToNext(query, currentSection, fragmentSupplier);
    }

    @Override
    public final Crawler next() {
        return next;
    }

    void checkSection(String currentSection) {
        if (!isOptional() && !section.equals(currentSection)) {
            throw new IllegalStateException("Unexpected token ^" + currentSection);
        }
    }
}
