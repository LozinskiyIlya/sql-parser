package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.FragmentCrawler;
import com.ecwid.parser.crawler.base.helper.CrawlContext;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.Sort;
import com.ecwid.parser.fragment.domain.Fragment;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@LexemeHandler(lexemes = {LEX_ASC, LEX_DESC, LEX_NULLS})
public class OrderConfigCrawler extends FragmentCrawler {

    @Override
    protected String lexAfterClause(CrawlContext context) {
        final var currentSection = context.getCurrentSection();
        final var query = context.getQuery();
        final var nextLex = context.getNextLex();
        String lex;
        if (LEX_NULLS.equals(currentSection)) {
            lex = setNullsAndReturnNext(query, nextLex);
        } else {
            lex = setDirectionAndReturnNext(query, currentSection, nextLex);
            if (LEX_NULLS.equals(lex)) {
                lex = setNullsAndReturnNext(query, nextLex);
            }
        }
        if (LEX_COMMA.equals(lex)) {
            final var sort = new Sort();
            query.getSorts().add(sort);
            return nextLex.get();
        }
        if (shouldDelegate(lex)) {
            delegate(new CrawlContext(query, lex, nextLex, context.getOpenBrackets()));
            return LEX_SEMICOLON;
        }
        return lex;
    }

    @Override
    protected void onFragment(Query query, Fragment fragment) {
        final var last = query.getSorts().getLast();
        if (last.getSortBy() == null) {
            last.setSortBy(fragment);
        } else {
            final var sort = new Sort();
            sort.setSortBy(fragment);
            query.getSorts().add(sort);
        }
    }

    private String setNullsAndReturnNext(Query query, Supplier<String> nextLex) {
        query.getSorts().getLast().setNulls(Sort.Nulls.valueOf(nextLex.get().toUpperCase()));
        return nextLex.get();
    }

    private String setDirectionAndReturnNext(Query query, String direction, Supplier<String> nextLex) {
        query.getSorts().getLast().setDirection(Sort.Direction.valueOf(direction.toUpperCase()));
        return nextLex.get();
    }
}
