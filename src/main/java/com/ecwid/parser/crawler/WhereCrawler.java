package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.ConditionCrawler;

import static com.ecwid.parser.Lexemes.LEX_HAVING;
import static com.ecwid.parser.Lexemes.LEX_WHERE;

@LexemeHandler(lexemes= {LEX_WHERE, LEX_HAVING})
public class WhereCrawler extends ConditionCrawler {
    public WhereCrawler() {
        super(
                (query, condition) -> query.getFilters().add(condition),
                (query, fragment) -> query.getFilters().getLast().addNextPart(fragment)
        );
    }
}