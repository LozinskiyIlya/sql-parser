package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
class LimitCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String limitKeyword, Supplier<String> nextFragmentSupplier) {
        final var limit = nextFragmentSupplier.get();
        query.setLimit(Integer.parseInt(limit));
        delegate(query, nextFragmentSupplier.get(), nextFragmentSupplier);
    }
}
