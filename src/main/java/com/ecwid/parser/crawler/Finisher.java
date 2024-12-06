package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Component
public class Finisher implements Crawler {
    @Override
    public Optional<Crawler> nextCrawler(String nextSection) {
        return Optional.empty();
    }

    @Override
    public void crawl(Query query, String currentSection, Supplier<String> fragments) {
        log.info("Query finished with {}", currentSection);
    }
}
