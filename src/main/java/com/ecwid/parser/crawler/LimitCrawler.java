package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
class LimitCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String limitKeyword, Supplier<String> fragments) {
        final var limit = fragments.get();
        query.setLimit(Integer.parseInt(limit));
        delegate(query, fragments.get(), fragments);
    }
}
