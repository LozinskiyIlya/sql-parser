package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.ConditionCrawler;
import org.springframework.stereotype.Component;

@Component
public class OnCrawler extends ConditionCrawler {

    public OnCrawler() {
        super(
                (query, condition) -> query.getJoins().getLast().getConditions().add(condition),
                (query, fragment) -> query.getJoins().getLast().getConditions().getLast().addOperand(fragment)
        );
    }
}