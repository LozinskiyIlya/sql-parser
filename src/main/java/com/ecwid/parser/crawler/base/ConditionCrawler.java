package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.fragment.Condition.Operator.operatorFullLexemes;

public abstract class ConditionCrawler extends FragmentCrawler {
    protected final BiConsumer<Query, Condition> onCondition;
    private final OperandCrawler operandCrawler;

    public ConditionCrawler(BiConsumer<Query, Condition> onCondition, BiConsumer<Query, Fragment> onOperand) {
        this.onCondition = onCondition;
        this.operandCrawler = new OperandCrawler(onOperand);
    }

    @Override
    protected String processClauseAndReturnNextLex(Query query, String clauseType, Supplier<String> nextLex) {
        final var condition = new Condition(Condition.ClauseType.valueOf(clauseType.toUpperCase()));
        onCondition.accept(query, condition);
        operandCrawler.crawl(query, nextLex.get(), nextLex);
        final var rightOperandFirstFragment = crawlForOperator(condition, nextLex);
        operandCrawler.crawl(query, rightOperandFirstFragment, nextLex);
        final var nextSection = nextLex.get();
        if (hasNextCondition(nextSection)) {
            return processClauseAndReturnNextLex(query, nextSection, nextLex);
        }
        return nextSection;
    }

    @Override
    protected void processFragment(Query query, Fragment fragment) {

    }


    private String crawlForOperator(Condition clause, Supplier<String> nextLex) {
        final var operatorParts = new LinkedList<String>();
        operatorParts.add(nextLex.get());
        crawlUntilAndReturnNext(
                fragment -> {
                    operatorParts.add(fragment);
                    return !isOperator(operatorParts);
                },
                fragment -> {
                },
                nextLex);
        final var nextFragment = operatorParts.removeLast();
        clause.setOperator(operatorFullLexemes.get(String.join(LEX_SPACE, operatorParts)));
        return nextFragment;
    }

    private boolean isOperator(List<String> parts) {
        return operatorFullLexemes.containsKey(String.join(LEX_SPACE, parts));
    }

    private boolean hasNextCondition(String fragment) {
        return LEX_AND.equals(fragment) || LEX_OR.equals(fragment);
    }

    @RequiredArgsConstructor
    private class OperandCrawler extends FragmentCrawler {

        private final BiConsumer<Query, Fragment> onFragment;


        @Override
        protected String processClauseAndReturnNextLex(Query query, String currentLex, Supplier<String> nextLex) {
            return currentLex;
        }

        @Override
        protected void processFragment(Query query, Fragment fragment) {
            onFragment.accept(query, fragment);
        }

        @Override
        public Optional<Crawler> nextCrawler(String currentSection) {
            return ConditionCrawler.this.nextCrawler(currentSection);
        }

        @Override
        protected boolean shouldDelegate(String nextSection) {
            return false;
        }

        @Override
        protected boolean crawlOnce() {
            return true;
        }
    }
}
