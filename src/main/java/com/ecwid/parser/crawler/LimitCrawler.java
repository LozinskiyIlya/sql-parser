package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_LIMIT;

public class LimitCrawler extends SectionAwareCrawler {

    public LimitCrawler() {
        super(null, LEX_LIMIT);
    }

    @Override
    void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        final var limit = fragmentSupplier.get();
        if (limit == null) {
            throw new IllegalStateException("Limit should be the last section in the query");
        }
        query.setLimit(Integer.parseInt(limit));
    }
}
