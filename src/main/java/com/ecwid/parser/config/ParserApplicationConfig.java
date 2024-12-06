package com.ecwid.parser.config;

import com.ecwid.parser.crawler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.ecwid.parser.Lexemes.*;

@Configuration
@ComponentScan(basePackages = "com.ecwid.parser")
public class ParserApplicationConfig {

    @Bean
    public Map<String, Crawler> sectionAgainstCrawler(
            ColumnCrawler columnCrawler,
            SourceCrawler sourceCrawler,
            JoinCrawler joinCrawler,
            WhereCrawler whereCrawler,
            JoinConditionCrawler joinConditionCrawler,
            LimitCrawler limitCrawler,
            OffsetCrawler offsetCrawler,
            QueryFinishedCrawler queryFinishedCrawler
    ) {
        final var sectionCrawlerMap = new HashMap<String, Crawler>();
        sectionCrawlerMap.put(LEX_SELECT, columnCrawler);
        sectionCrawlerMap.put(LEX_FROM, sourceCrawler);
        sectionCrawlerMap.put(LEX_JOIN, joinCrawler);
        sectionCrawlerMap.put(LEX_WHERE, whereCrawler);
        sectionCrawlerMap.put(LEX_HAVING, whereCrawler);

        sectionCrawlerMap.put(LEX_INNER, joinCrawler);
        sectionCrawlerMap.put(LEX_LEFT, joinCrawler);
        sectionCrawlerMap.put(LEX_RIGHT, joinCrawler);
        sectionCrawlerMap.put(LEX_FULL, joinCrawler);
        sectionCrawlerMap.put(LEX_CROSS, joinCrawler);
        sectionCrawlerMap.put(LEX_NATURAL, joinCrawler);
        sectionCrawlerMap.put(LEX_ON, joinConditionCrawler);

        sectionCrawlerMap.put(LEX_GROUP, null);
        sectionCrawlerMap.put(LEX_ORDER, null);
        sectionCrawlerMap.put(LEX_LIMIT, limitCrawler);
        sectionCrawlerMap.put(LEX_OFFSET, offsetCrawler);

        sectionCrawlerMap.put(LEX_SEMICOLON, queryFinishedCrawler);

        return sectionCrawlerMap;
    }
}
