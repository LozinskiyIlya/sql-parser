package com.ecwid.parser.crawler;

import com.ecwid.parser.config.TriggerMeOn;
import com.ecwid.parser.crawler.base.NoClauseProcessCrawler;

import static com.ecwid.parser.Lexemes.LEX_SELECT;

@TriggerMeOn(lexemes = LEX_SELECT)
public class ColumnCrawler extends NoClauseProcessCrawler {
    public ColumnCrawler() {
        super((query, fragment) -> query.getColumns().add(fragment));
    }
}