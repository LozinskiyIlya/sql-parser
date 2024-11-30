package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
class OffsetCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String keywordOffset, Supplier<String> fragmentSupplier) {
        final var offset = fragmentSupplier.get();
        query.setOffset(Integer.parseInt(offset));
        delegateToNextCrawler(query, fragmentSupplier.get(), fragmentSupplier);
    }
}
