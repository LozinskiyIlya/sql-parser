package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.CONDITION_SEPARATORS;
import static com.ecwid.parser.Lexemes.LEX_SPACE;
import static com.ecwid.parser.fragment.Condition.Operator.operatorFullLexemes;

public abstract class ConditionCrawler extends FragmentCrawler {
    protected final BiConsumer<Query, Condition> onCondition;

    public ConditionCrawler(BiConsumer<Query, Condition> onCondition, BiConsumer<Query, Fragment> onOperand) {
        this.onCondition = onCondition;
    }

    @Override
    protected String processClauseAndReturnNextLex(Query query, String clauseType, Supplier<String> nextLex) {
        final var condition = new Condition(Condition.ClauseType.valueOf(clauseType.toUpperCase()));
        onCondition.accept(query, condition);
        return nextLex.get();
    }

    @Override
    protected void processFragment(Query query, Fragment fragment) {
    }

    private String crawlForOperator(Condition clause, String operatorFirstLex, Supplier<String> nextLex) {
        final var operatorParts = new LinkedList<String>();
        operatorParts.add(operatorFirstLex);
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

    private boolean hasNextCondition(String lex) {
        return CONDITION_SEPARATORS.contains(lex);
    }
}
