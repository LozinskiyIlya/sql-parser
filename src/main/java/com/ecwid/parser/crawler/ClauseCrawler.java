package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.clause.WhereClause;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Component
class ClauseCrawler extends SectionAwareCrawler {


    public ClauseCrawler() {
        super(null, LEX_WHERE);
    }

    @Override
    void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        String nextFragment;
        WhereClause whereClause = null;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (QUERY_SECTIONS.contains(nextFragment)) {
                delegateToNext(query, nextFragment, fragmentSupplier);
                return;
            }
            if (LEX_SELECT.equals(nextFragment)) {
//                columnCrawler.addFragment(query, nextFragment, fragmentSupplier);
            }
            query.getWhereClauses().add(whereClause);
        }
    }

    @Override
    boolean isOptional() {
        return true;
    }
}
