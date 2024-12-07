package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.NoClauseProcessCrawler;
import org.springframework.stereotype.Component;

@Component
public class ColumnCrawler extends NoClauseProcessCrawler {
    public ColumnCrawler() {
        super((query, fragment) -> query.getColumns().add(fragment));
    }
}