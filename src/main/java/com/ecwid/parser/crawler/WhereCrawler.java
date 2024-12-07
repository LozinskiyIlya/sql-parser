package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.ConditionCrawler;
import org.springframework.stereotype.Component;

@Component
public class WhereCrawler extends ConditionCrawler {
    public WhereCrawler() {
        super(
                (query, condition) -> query.getFilters().add(condition),
                (query, fragment) -> query.getFilters().getLast().addOperand(fragment)
        );
    }
}