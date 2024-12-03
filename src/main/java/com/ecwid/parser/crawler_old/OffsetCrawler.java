package com.ecwid.parser.crawler_old;

import com.ecwid.parser.fragment.enity.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
class OffsetCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String offsetKeyword, Supplier<String> nextFragmentSupplier) {
        final var offset = nextFragmentSupplier.get();
        query.setOffset(Integer.parseInt(offset));
        delegate(query, nextFragmentSupplier.get(), nextFragmentSupplier);
    }
}
