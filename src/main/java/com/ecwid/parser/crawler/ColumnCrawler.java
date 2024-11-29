package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_SELECT;
import static com.ecwid.parser.Lexemes.QUERY_SECTIONS;

public class ColumnCrawler extends SectionAwareCrawler {

    public ColumnCrawler(SourceCrawler next) {
        super(next, LEX_SELECT);
    }

    @Override
    public void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (QUERY_SECTIONS.contains(nextFragment)) {
                delegateToNextCrawler(query, nextFragment, fragmentSupplier);
                return;
            }
            query.getColumns().add(nextFragment);
        }
    }
}
