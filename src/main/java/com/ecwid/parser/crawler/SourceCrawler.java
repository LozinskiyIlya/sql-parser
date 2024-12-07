package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.NoClauseProcessCrawler;
import com.ecwid.parser.fragment.domain.Source;
import org.springframework.stereotype.Component;

@Component
public class SourceCrawler extends NoClauseProcessCrawler {

    public SourceCrawler() {
        super((query, fragment) -> query.getSources().add((Source) fragment));
    }

    @Override
    protected boolean crawlsForSources() {
        return true;
    }
}
