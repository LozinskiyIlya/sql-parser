package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class NoopCrawler implements Crawler {
    @Override
    public Crawler nextCrawler(String nextSection) {
        return null;
    }

    @Override
    public void crawl(Query query, String currentSection, Supplier<String> nextFragmentSupplier) {
        // do nothing
    }
}