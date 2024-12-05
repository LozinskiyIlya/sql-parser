package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class QueryFinishedCrawler implements Crawler {
    @Override
    public Crawler nextCrawler(String nextSection) {
        return null;
    }

    @Override
    public void crawl(Query query, String currentSection, Supplier<String> nextFragmentSupplier) {
        log.info("Query finished with {}", currentSection);
    }
}
