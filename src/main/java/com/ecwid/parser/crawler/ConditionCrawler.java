package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.condition.Condition;
import com.ecwid.parser.fragment.condition.ConstantListOperand;
import com.ecwid.parser.fragment.condition.ConstantOperand;
import com.ecwid.parser.fragment.condition.Operand;
import com.ecwid.parser.fragment.domain.Column;
import com.ecwid.parser.fragment.domain.NameAliasPair;
import com.ecwid.parser.fragment.domain.Query;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.fragment.condition.Condition.Operator.operatorFullLexemes;

public abstract class ConditionCrawler extends SectionAwareCrawler implements CanHaveFunction {

    protected BiConsumer<Query, Condition> addToQuery;

    @Override
    public void crawl(Query query, String clauseName, Supplier<String> fragments) {
        final var condition = new Condition(Condition.ClauseType.valueOf(clauseName.toUpperCase()));
        final var leftOperandFirstFragment = fragments.get();
        final var operatorFirstFragment = crawlForOperand(condition, leftOperandFirstFragment, fragments, null);
        final var rightOperandFirstFragment = crawlForOperator(condition, operatorFirstFragment, fragments);
        final var nexFragment = crawlForOperand(condition, rightOperandFirstFragment, fragments, condition.getOperator());
        addToQuery.accept(query, condition);
        if (hasNextCondition(nexFragment)) {
            crawl(query, nexFragment, fragments);
            return;
        }
        delegate(query, nexFragment, fragments);
    }


    private String crawlForOperand(
            Condition clause,
            String fragment,
            Supplier<String> fragments,
            Condition.Operator operator
    ) {
        Operand operand;
        if (LEX_OPEN_BRACKET.equals(fragment)) {
            fragment = fragments.get();
        }
        if (LEX_SELECT.equals(fragment)) {
            operand = new Query();
            nextCrawler(fragment).orElseThrow().crawl((Query) operand, fragment, fragments);
            fragment = fragments.get();
        } else if (operator != null && LEX_IN.equals(operator.getFullLexeme())) {
            operand = new ConstantListOperand();
            final var values = ((ConstantListOperand) operand).getValues();
            values.add(fragment);
            fragment = crawlUntilAndReturnNext(
                    this::shouldDelegate,
                    fr -> {
                        if (LEX_COMMA.equals(fr) || LEX_CLOSE_BRACKET.equals(fr)) {
                            return;
                        }
                        values.add(fr);
                    },
                    fragments);
        } else if (isConstant(fragment)) {
            operand = new ConstantOperand(fragment);
            clause.setNextOperand(operand);
            fragment = fragments.get();
        } else {
            final var canBeFunction = new NameAliasPair();
            canBeFunction.push(fragment);
            fragment = fragments.get();
            if (LEX_OPEN_BRACKET.equals(fragment)) {
                operand = new Column(getFunctionSignature(canBeFunction, fragments), null);
                fragment = fragments.get();
            } else {
                operand = new Column(canBeFunction.getFirst(), null);
            }
        }
        clause.setNextOperand(operand);
        return fragment;
    }

    private String crawlForOperator(Condition clause, String firstFragment, Supplier<String> fragments) {
        final var operatorParts = new LinkedList<String>();
        operatorParts.add(firstFragment);
        crawlUntilAndReturnNext(
                fragment -> {
                    operatorParts.add(fragment);
                    return !isOperator(operatorParts);
                },
                fragment -> {
                },
                fragments);
        final var nextFragment = operatorParts.removeLast();
        clause.setOperator(operatorFullLexemes.get(String.join(LEX_SPACE, operatorParts)));
        return nextFragment;
    }

    private boolean isOperator(List<String> parts) {
        return operatorFullLexemes.containsKey(String.join(LEX_SPACE, parts));
    }

    private boolean isConstant(String fragment) {
        return isQuotedString(fragment) || isConstantNumber(fragment) || isNullConstant(fragment);
    }

    private boolean isQuotedString(String fragment) {
        return fragment.startsWith(LEX_SINGLE_QUOTE) && fragment.endsWith(LEX_SINGLE_QUOTE);
    }

    private boolean isNullConstant(String fragment) {
        return LEX_NULL.equals(fragment);
    }

    private boolean isConstantNumber(String fragment) {
        return fragment.matches("\\d+");
    }

    private boolean hasNextCondition(String fragment) {
        return LEX_AND.equals(fragment) || LEX_OR.equals(fragment);
    }
}
