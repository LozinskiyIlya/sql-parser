package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;



@Component
class OffsetCrawler extends SectionAwareCrawler {

    @Override
    void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        final var limit = fragmentSupplier.get();
        if (limit == null) {
            throw new IllegalStateException("Limit should be the last section in the query");
        }
        query.setLimit(Integer.parseInt(limit));
    }
}
