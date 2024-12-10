package com.ecwid.parser.crawler;

import com.ecwid.parser.config.TriggerMeOn;
import com.ecwid.parser.crawler.base.NoClauseProcessCrawler;
import com.ecwid.parser.fragment.domain.Source;

import static com.ecwid.parser.Lexemes.LEX_FROM;

@TriggerMeOn(lexemes = LEX_FROM)
public class SourceCrawler extends NoClauseProcessCrawler {

    public SourceCrawler() {
        super((query, fragment) -> query.getSources().add((Source) fragment));
    }

    @Override
    protected boolean crawlsForSources() {
        return true;
    }
}
