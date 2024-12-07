package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class OffsetCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String offsetKeyword, Supplier<String> fragments) {
        final var offset = fragments.get();
        query.setOffset(Integer.parseInt(offset));
        delegate(query, fragments.get(), fragments);
    }
}
