package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.clause.WhereClause;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

public class ClauseCrawler extends SectionAwareCrawler {

    Crawler columnCrawler = new ColumnCrawler(new SourceCrawler(this));

    public ClauseCrawler(Crawler next) {
        super(next, LEX_WHERE);
    }

    @Override
    void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        String nextFragment;
        WhereClause whereClause = null;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (QUERY_SECTIONS.contains(nextFragment)) {
                delegateToNextCrawler(query, nextFragment, fragmentSupplier);
                return;
            }
            if (LEX_SELECT.equals(nextFragment)) {
                columnCrawler.addFragment(query, nextFragment, fragmentSupplier);

            }
            query.getWhereClauses().add(whereClause);
        }
    }
}
