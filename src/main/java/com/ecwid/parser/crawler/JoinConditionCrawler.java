package com.ecwid.parser.crawler;

import org.springframework.stereotype.Component;

@Component
public class JoinConditionCrawler extends ConditionCrawler {
    {
        addToQuery = (query, condition) -> query.getJoins().getLast().getConditions().add(condition);
    }
}