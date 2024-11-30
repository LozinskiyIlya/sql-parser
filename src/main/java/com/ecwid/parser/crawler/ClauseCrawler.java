package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.clause.QueryOperand;
import com.ecwid.parser.fragment.clause.WhereClause;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_SELECT;

@Component
public class ClauseCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        var nextFragment = fragmentSupplier.get();
        final var clause = new WhereClause();
        if (LEX_SELECT.equals(nextFragment)) {
            final var nestedQuery = new Query();
            final var operand = new QueryOperand(nestedQuery);
            selectCrawler(nextFragment).crawl(nestedQuery, nextFragment, fragmentSupplier);
            clause.setNextOperand(operand);
        } else if (QUERY_SECTIONS.containsKey(nextFragment)) {
            delegateToNextCrawler(query, nextFragment, fragmentSupplier);
        }
    }
}
