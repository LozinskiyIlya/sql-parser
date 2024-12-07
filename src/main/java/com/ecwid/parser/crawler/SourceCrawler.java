package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Source;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class SourceCrawler extends FragmentCrawler {

    @Override
    protected void addFragmentToQuery(Query query, Fragment fragment) {
        query.getSources().add((Source) fragment);
    }

    @Override
    protected String addClauseToQueryAndReturnNextLex(Query query, String currentSection, Supplier<String> nextLex) {
        return nextLex.get();
    }

    @Override
    protected boolean isCrawlingForSources() {
        return true;
    }
}
