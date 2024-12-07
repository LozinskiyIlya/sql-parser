package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ColumnCrawler extends FragmentCrawler {

    public ColumnCrawler() {
        super((query, fragment) -> query.getColumns().add(fragment));
    }

    @Override
    protected String crawlClauseAndReturnNextLex(Query query, String currentSection, Supplier<String> nextLex) {
        return nextLex.get();
    }
}