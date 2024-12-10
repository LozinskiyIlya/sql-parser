package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.CONDITION_SEPARATORS;

@Slf4j
@RequiredArgsConstructor
public abstract class ConditionCrawler extends FragmentCrawler {
    protected final BiConsumer<Query, Condition> onCondition;
    protected final BiConsumer<Query, Fragment> onFragment;

    @Override
    protected String processClauseAndReturnNextLex(Query query, String curLex, Supplier<String> nextLex) {
        return Arrays.stream(Condition.ClauseType.values())
                .map(Enum::name)
                .filter(curLex::equalsIgnoreCase)
                .findFirst()
                .map(Condition.ClauseType::valueOf)
                .map(Condition::new)
                .map(condition -> {
                    onCondition.accept(query, condition);
                    return nextLex.get();
                })
                .orElse(curLex);
    }

    @Override
    protected void processFragment(Query query, Fragment fragment) {
        log.info("Processing fragment {}", fragment);
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
