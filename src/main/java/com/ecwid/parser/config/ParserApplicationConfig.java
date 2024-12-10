package com.ecwid.parser.config;

import com.ecwid.parser.crawler.*;
import com.ecwid.parser.crawler.base.Crawler;
import com.ecwid.parser.crawler.Finisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ecwid.parser.Lexemes.*;

@Configuration
@ComponentScan(basePackages = "com.ecwid.parser")
@RequiredArgsConstructor
public class ParserApplicationConfig {

    private final List<Crawler> crawlers;

    @Bean
    public Map<String, Crawler> sectionAgainstCrawler() {
        final var sectionCrawlerMap = new HashMap<String, Crawler>();
        sectionCrawlerMap.put(LEX_SELECT, getByClass(ColumnCrawler.class));
        sectionCrawlerMap.put(LEX_FROM, getByClass(SourceCrawler.class));

        final var joinCrawler = getByClass(JoinCrawler.class);
        sectionCrawlerMap.put(LEX_JOIN, joinCrawler);
        sectionCrawlerMap.put(LEX_INNER, joinCrawler);
        sectionCrawlerMap.put(LEX_LEFT, joinCrawler);
        sectionCrawlerMap.put(LEX_RIGHT, joinCrawler);
        sectionCrawlerMap.put(LEX_FULL, joinCrawler);
        sectionCrawlerMap.put(LEX_CROSS, joinCrawler);
        sectionCrawlerMap.put(LEX_NATURAL, joinCrawler);
        sectionCrawlerMap.put(LEX_ON, getByClass(OnCrawler.class));

        final var whereCrawler = getByClass(WhereCrawler.class);
        sectionCrawlerMap.put(LEX_WHERE, whereCrawler);
        sectionCrawlerMap.put(LEX_HAVING, whereCrawler);

        sectionCrawlerMap.put(LEX_GROUP, getByClass(GroupCrawler.class));
        sectionCrawlerMap.put(LEX_ORDER, null);
        sectionCrawlerMap.put(LEX_LIMIT, getByClass(LimitCrawler.class));
        sectionCrawlerMap.put(LEX_OFFSET, getByClass(OffsetCrawler.class));

        final var finisher = getByClass(Finisher.class);
        sectionCrawlerMap.put(LEX_SEMICOLON, finisher);
        sectionCrawlerMap.put(null, finisher);
        sectionCrawlerMap.put("", finisher);

        return sectionCrawlerMap;
    }

    private Crawler getByClass(Class<? extends Crawler> clazz) {
        return crawlers.stream()
                .filter(clazz::isInstance)
                .findFirst()
                .orElseThrow();
    }
}
