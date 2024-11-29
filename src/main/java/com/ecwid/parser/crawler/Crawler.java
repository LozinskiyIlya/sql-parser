package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;

import java.util.function.Supplier;

public interface Crawler {

    Crawler next();

    void addFragment(Query query, String currentSection, Supplier<String> fragmentSupplier);

     default void delegateToNextCrawler(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        if (next() != null) {
            next().addFragment(query, currentSection, fragmentSupplier);
        }
    }
}
