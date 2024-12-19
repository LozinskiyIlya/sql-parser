package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.FragmentCrawler;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class NestedJoinCrawler extends FragmentCrawler {


    @Override
    protected String processClauseAndReturnNextLex(Query query, String currentSection, Supplier<String> nextLex) {
        return currentSection;
    }

    @Override
    protected void processFragment(Query query, Fragment fragment) {
        query.getSources().add((Source) fragment);
    }

    @Override
    protected boolean crawlsForSources() {
        return true;
    }
}
