package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.CONDITION_SEPARATORS;

@RequiredArgsConstructor
public abstract class ConditionCrawler extends FragmentCrawler {
    protected final BiConsumer<Query, Condition> onCondition;
    protected final BiConsumer<Query, Fragment> onFragment;

    @Override
    protected String processClauseAndReturnNextLex(Query query, String clauseType, Supplier<String> nextLex) {
        final var condition = new Condition(Condition.ClauseType.valueOf(clauseType.toUpperCase()));
        onCondition.accept(query, condition);
        final var next = nextLex.get();
        if (next == null) {
            throw new IllegalStateException("Unexpected end of query after " + clauseType);
        }
        return next;
    }

    @Override
    protected void processFragment(Query query, Fragment fragment) {
        System.out.println("processFragment");
        System.out.println(fragment);
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
