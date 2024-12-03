package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Column;
import org.springframework.stereotype.Component;

@Component
public class ColumnCrawler extends SectionAwareCrawler {

    {
        crawlUntil = fragment -> false;
        addToQuery = (query, fragment) -> query.getColumns().add(new Column(fragment, null));
    }
}
