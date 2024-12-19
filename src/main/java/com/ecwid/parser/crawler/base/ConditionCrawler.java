package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.Condition.ClauseType;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.BiConsumer;

import static com.ecwid.parser.Lexemes.CONDITION_SEPARATORS;

@RequiredArgsConstructor
public abstract class ConditionCrawler extends FragmentCrawler {
    protected final BiConsumer<Query, Condition> onCondition;
    protected final BiConsumer<Query, Fragment> onFragment;

    @Override
    protected String lexAfterClause(CrawlContext context) {
        final var query = context.query();
        final var curLex = context.currentSection();
        final var nextLex = context.nextLexSupplier();
        return ClauseType.fromString(curLex)
                .map(Condition::new)
                .map(condition -> {
                    onCondition.accept(query, condition);
                    return nextLex.get();
                })
                .orElse(curLex);
    }

    @Override
    protected void onFragment(Query query, Fragment fragment) {
        onFragment.accept(query, fragment);
    }

    @Override
    public Optional<Crawler> nextCrawler(String currentSection) {
        if (hasNextCondition(currentSection)) {
            return Optional.of(this);
        }
        return super.nextCrawler(currentSection);
    }

    @Override
    protected boolean shouldDelegate(String nextSection) {
        return hasNextCondition(nextSection) || super.shouldDelegate(nextSection);
    }

    private boolean hasNextCondition(String lex) {
        return lex != null && CONDITION_SEPARATORS.contains(lex);
    }
}
