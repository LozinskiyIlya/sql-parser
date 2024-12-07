package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ColumnCrawler extends FragmentCrawler {

    @Override
    protected void addFragmentToQuery(Query query, Fragment fragment) {
        query.getColumns().add(fragment);
    }

    @Override
    protected String addClauseToQueryAndReturnNextLex(Query query, String currentSection, Supplier<String> nextLex) {
        return nextLex.get();
    }
}