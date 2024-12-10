package com.ecwid.parser.crawler;

import com.ecwid.parser.config.TriggerMeOn;
import com.ecwid.parser.crawler.base.ConditionCrawler;

import static com.ecwid.parser.Lexemes.LEX_ON;

@TriggerMeOn(lexemes = LEX_ON)
public class OnCrawler extends ConditionCrawler {

    public OnCrawler() {
        super(
                (query, condition) -> query.getJoins().getLast().getConditions().add(condition),
                (query, fragment) -> query.getJoins().getLast().getConditions().getLast().addNextPart(fragment)
        );
    }
}