package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.IntegerCrawler;
import com.ecwid.parser.fragment.Query;

import static com.ecwid.parser.Lexemes.LEX_OFFSET;

@LexemeHandler(lexemes = LEX_OFFSET)
public class OffsetCrawler extends IntegerCrawler {

    public OffsetCrawler() {
        super(Query::setOffset, LEX_OFFSET);
    }
}
