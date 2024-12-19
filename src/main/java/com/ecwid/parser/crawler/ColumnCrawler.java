package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.SkipClauseCrawler;

import static com.ecwid.parser.Lexemes.LEX_SELECT;

@LexemeHandler(lexemes = LEX_SELECT)
public class ColumnCrawler extends SkipClauseCrawler {
    public ColumnCrawler() {
        super((query, fragment) -> query.getColumns().add(fragment));
    }
}