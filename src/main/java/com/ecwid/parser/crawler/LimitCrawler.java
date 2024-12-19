package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.IntegerCrawler;
import com.ecwid.parser.fragment.Query;

import static com.ecwid.parser.Lexemes.LEX_LIMIT;

@LexemeHandler(lexemes = LEX_LIMIT)
public class LimitCrawler extends IntegerCrawler {

    public LimitCrawler() {
        super(Query::setLimit, LEX_LIMIT);
    }
}
