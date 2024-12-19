package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.SkipClauseCrawler;

import static com.ecwid.parser.Lexemes.LEX_GROUP;

@LexemeHandler(lexemes = LEX_GROUP)
public class GroupCrawler extends SkipClauseCrawler {

    public GroupCrawler() {
        super((query, fragment) -> query.getGroupings().add(fragment));
    }
}
