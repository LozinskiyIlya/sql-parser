package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import lombok.AllArgsConstructor;

import java.util.function.Supplier;


@AllArgsConstructor
public abstract class SectionAwareCrawler implements Crawler {
    private Crawler next;
    private String section;

    abstract void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier);

    public void addFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        checkSection(currentSection);
        addQueryFragment(query, currentSection, fragmentSupplier);
    }

    private void checkSection(String currentSection) {
        if (!section.equals(currentSection)) {
            throw new IllegalStateException("Unexpected token ^" + currentSection);
        }
    }

    @Override
    public final void delegateToNextCrawler(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        Crawler.super.delegateToNextCrawler(query, currentSection, fragmentSupplier);
    }

    @Override
    public final Crawler next() {
        return next;
    }
}
