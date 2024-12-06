package com.ecwid.parser.crawler;

import org.springframework.stereotype.Component;

@Component
public class WhereCrawler extends ConditionCrawler {
    {
        addToQuery = (query, condition) -> query.getFilters().add(condition);
    }
}