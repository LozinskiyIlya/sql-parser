package com.ecwid.parser.crawler;

import com.ecwid.parser.config.TriggerMeOn;
import com.ecwid.parser.crawler.base.Crawler;
import com.ecwid.parser.crawler.base.FragmentCrawler;
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

@TriggerMeOn(lexemes = {
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
    protected void processFragment(Query query, Fragment fragment) {
        query.getJoins().getLast().setSource((Source) fragment);
    }

    @Override
    protected String processClauseAndReturnNextLex(Query query, String currentSection, Supplier<String> nextLex) {
        final var join = new Join();
        query.getJoins().add(join);
        return crawlForJoinType(join, currentSection, nextLex);
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
        join.setType(joinTypeFullLexemes.get(String.join(LEX_SPACE, joinTypeParts)));
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
