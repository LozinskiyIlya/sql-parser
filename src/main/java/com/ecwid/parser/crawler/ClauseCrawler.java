package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Column;
import com.ecwid.parser.fragment.enity.Query;
import com.ecwid.parser.fragment.clause.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ecwid.parser.Lexemes.*;

@Component
public class ClauseCrawler extends SectionAwareCrawler {

    private static final Map<String, WhereClause.Operator> operatorFullLexemes = Arrays.stream(WhereClause.Operator.values())
            .collect(Collectors.toMap(WhereClause.Operator::getFullLexeme, Function.identity()));

    @Override
    public void crawl(Query query, String clauseName, Supplier<String> fragmentSupplier) {
        final var clause = new WhereClause(WhereClause.ClauseType.valueOf(clauseName.toUpperCase()));
        final var leftOperandFirstFragment = fragmentSupplier.get();
        final var operatorFirstFragment = crawlForOperand(clause, leftOperandFirstFragment, fragmentSupplier, null);
        final var rightOperandFirstFragment = crawlForOperator(clause, operatorFirstFragment, fragmentSupplier);
        final var nexFragment = crawlForOperand(clause, rightOperandFirstFragment, fragmentSupplier, clause.getOperator());
        query.getWhereClauses().add(clause);
        delegate(query, nexFragment, fragmentSupplier);
    }


    private String crawlForOperand(
            WhereClause clause,
            String firstFragment,
            Supplier<String> fragmentSupplier,
            WhereClause.Operator operator
    ) {
        Operand operand;
        var fragment = String.copyValueOf(firstFragment.toCharArray());
        if (LEX_OPEN_BRACKET.equals(fragment)) {
            fragment = fragmentSupplier.get();
        }
        if (LEX_SELECT.equals(fragment)) {
            operand = new Query();
            selectCrawler(fragment).crawl((Query) operand, fragment, fragmentSupplier);
            fragment = fragmentSupplier.get();
        } else if (operator != null && LEX_IN.equals(operator.getFullLexeme())) {
            operand = new ConstantListOperand();
            final var values = ((ConstantListOperand) operand).getValues();
            values.add(fragment);
            fragment = crawlUntilAndReturnNext(this::shouldDelegate, values::add, fragmentSupplier);
        } else if (isConstant(fragment)) {
            operand = new ConstantOperand(fragment);
            clause.setNextOperand(operand);
            fragment = fragmentSupplier.get();
        } else {
            operand = new Column(fragment, null);
            fragment = fragmentSupplier.get();
        }
        clause.setNextOperand(operand);
        return fragment;
    }

    private String crawlForOperator(WhereClause clause, String firstFragment, Supplier<String> fragmentSupplier) {
        final var operatorParts = new LinkedList<String>();
        operatorParts.add(firstFragment);
        crawlUntilAndReturnNext(
                fragment -> {
                    operatorParts.add(fragment);
                    return !isOperator(operatorParts);
                },
                fragment -> {
                },
                fragmentSupplier);
        final var nextFragment = operatorParts.removeLast();
        clause.setOperator(operatorFullLexemes.get(String.join("", operatorParts)));
        return nextFragment;
    }

    private boolean isOperator(List<String> parts) {
        return operatorFullLexemes.containsKey(String.join("", parts));
    }

    private boolean isConstant(String fragment) {
        return isQuotedString(fragment) || isConstantNumber(fragment);
    }

    private boolean isQuotedString(String fragment) {
        return fragment.startsWith(LEX_SINGLE_QUOTE) && fragment.endsWith(LEX_SINGLE_QUOTE);
    }

    private boolean isConstantNumber(String fragment) {
        return fragment.matches("\\d+");
    }
}
