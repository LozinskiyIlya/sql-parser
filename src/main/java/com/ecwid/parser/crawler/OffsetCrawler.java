package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_OFFSET;


@Component
class OffsetCrawler extends SectionAwareCrawler {

    public OffsetCrawler() {
        super(null, LEX_OFFSET);
    }

    @Override
    void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        final var limit = fragmentSupplier.get();
        if (limit == null) {
            throw new IllegalStateException("Limit should be the last section in the query");
        }
        query.setLimit(Integer.parseInt(limit));
    }

    @Override
    boolean isOptional() {
        return true;
    }
}
