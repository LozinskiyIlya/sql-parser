package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Column;
import com.ecwid.parser.fragment.enity.Query;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Component
public class ColumnCrawler extends SectionAwareCrawler implements ListCrawler {

    {
        crawlUntil = fragment -> false;
        addToQuery = ListCrawler.super.addToQuery();
    }

    @Override
    public BiConsumer<Query, String> onListItem() {
        return (query, fragment) -> query.getColumns().add(new Column(fragment, null));
    }
}
