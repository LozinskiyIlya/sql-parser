package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.Crawler;
import com.ecwid.parser.crawler.base.FragmentCrawler;
import com.ecwid.parser.crawler.base.helper.CrawlContext;
import com.ecwid.parser.fragment.Join;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Source;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.fragment.Join.JoinType.joinTypeFullLexemes;

@LexemeHandler(lexemes = {
        LEX_INNER,
        LEX_OUTER,
        LEX_LEFT,
        LEX_RIGHT,
        LEX_FULL,
        LEX_CROSS,
        LEX_NATURAL,
        LEX_JOIN
})
@RequiredArgsConstructor
public class JoinCrawler extends FragmentCrawler {

    private final NestedJoinCrawler nestedJoinCrawler;

    @Override
    protected boolean crawlsForSources() {
        return true;
    }

    @Override
    protected void onFragment(Query query, Fragment fragment) {
        query.getJoins().getLast().setSource((Source) fragment);
    }

    @Override
    protected String lexAfterClause(CrawlContext context) {
        final var join = new Join();
        context.getQuery().getJoins().add(join);
        return crawlForJoinType(join, context.getCurrentSection(), context.getNextLexSupplier());
    }

    private String crawlForJoinType(Join join, String joinFirstLex, Supplier<String> nextLex) {
        final var joinTypeParts = new LinkedList<String>();
        joinTypeParts.add(joinFirstLex);
        final var lexAfterJoin = crawlUntilAndReturnNext(
                lex -> {
                    if (joinTypeParts.contains(LEX_JOIN)) {
                        return true;
                    }
                    joinTypeParts.add(lex);
                    return false;
                },
                lex -> {
                },
                nextLex);
        join.setJoinType(joinTypeFullLexemes.get(String.join(LEX_SPACE, joinTypeParts)));
        return lexAfterJoin;
    }

    @Override
    public Optional<Crawler> nextCrawler(String currentSection) {
        final var crawler = super.nextCrawler(currentSection);
        if (crawler.isPresent()) {
            return crawler;
        } else {
            return Optional.of(nestedJoinCrawler);
        }
    }
}
