package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.SkipClauseCrawler;
import com.ecwid.parser.fragment.domain.Source;

import static com.ecwid.parser.Lexemes.LEX_FROM;

@LexemeHandler(lexemes = LEX_FROM)
public class SourceCrawler extends SkipClauseCrawler {

    public SourceCrawler() {
        super((query, fragment) -> query.getSources().add((Source) fragment));
    }

    @Override
    protected boolean crawlsForSources() {
        return true;
    }
}
