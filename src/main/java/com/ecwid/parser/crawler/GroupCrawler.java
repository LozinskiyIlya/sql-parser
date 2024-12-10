package com.ecwid.parser.crawler;

import com.ecwid.parser.config.TriggerMeOn;
import com.ecwid.parser.crawler.base.NoClauseProcessCrawler;

import static com.ecwid.parser.Lexemes.LEX_GROUP;

@TriggerMeOn(lexemes = LEX_GROUP)
public class GroupCrawler extends NoClauseProcessCrawler {

    public GroupCrawler() {
        super((query, fragment) -> query.getGroupings().add(fragment));
    }
}
