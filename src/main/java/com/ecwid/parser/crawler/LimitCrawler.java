package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
class LimitCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String limitKeyword, Supplier<String> fragmentSupplier) {
        final var limit = fragmentSupplier.get();
        query.setLimit(Integer.parseInt(limit));
        delegateToNextCrawler(query, fragmentSupplier.get(), fragmentSupplier);
    }
}
