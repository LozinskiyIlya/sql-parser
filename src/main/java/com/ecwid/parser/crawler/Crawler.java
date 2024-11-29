package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;

import java.util.function.Supplier;

interface Crawler {

    Crawler next();

    void addFragment(Query query, String currentSection, Supplier<String> fragmentSupplier);

    default void delegateToNext(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        if (next() != null) {
            next().addFragment(query, currentSection, fragmentSupplier);
        }
    }
}
