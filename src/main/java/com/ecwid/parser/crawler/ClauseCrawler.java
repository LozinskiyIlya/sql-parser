package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.clause.ConstantListOperand;
import com.ecwid.parser.fragment.clause.ConstantOperand;
import com.ecwid.parser.fragment.clause.Operand;
import com.ecwid.parser.fragment.clause.WhereClause;
import com.ecwid.parser.fragment.domain.Column;
import com.ecwid.parser.fragment.domain.Query;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.fragment.clause.WhereClause.Operator.operatorFullLexemes;

@Component
public class ClauseCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String clauseName, Supplier<String> nextFragmentSupplier) {
        final var clause = new WhereClause(WhereClause.ClauseType.valueOf(clauseName.toUpperCase()));
        final var leftOperandFirstFragment = nextFragmentSupplier.get();
        final var operatorFirstFragment = crawlForOperand(clause, leftOperandFirstFragment, nextFragmentSupplier, null);
        final var rightOperandFirstFragment = crawlForOperator(clause, operatorFirstFragment, nextFragmentSupplier);
        final var nexFragment = crawlForOperand(clause, rightOperandFirstFragment, nextFragmentSupplier, clause.getOperator());
        query.getFilters().add(clause);
        delegate(query, nexFragment, nextFragmentSupplier);
    }


    private String crawlForOperand(
            WhereClause clause,
            String firstFragment,
            Supplier<String> nextFragmentSupplier,
            WhereClause.Operator operator
    ) {
        Operand operand;
        var fragment = String.copyValueOf(firstFragment.toCharArray());
        if (LEX_OPEN_BRACKET.equals(fragment)) {
            fragment = nextFragmentSupplier.get();
        }
        if (LEX_SELECT.equals(fragment)) {
            operand = new Query();
            nextCrawler(fragment).crawl((Query) operand, fragment, nextFragmentSupplier);
            fragment = nextFragmentSupplier.get();
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
                    nextFragmentSupplier);
        } else if (isConstant(fragment)) {
            operand = new ConstantOperand(fragment);
            clause.setNextOperand(operand);
            fragment = nextFragmentSupplier.get();
        } else {
            operand = new Column(fragment, null);
            fragment = nextFragmentSupplier.get();
        }
        clause.setNextOperand(operand);
        return fragment;
    }

    private String crawlForOperator(WhereClause clause, String firstFragment, Supplier<String> nextFragmentSupplier) {
        final var operatorParts = new LinkedList<String>();
        operatorParts.add(firstFragment);
        crawlUntilAndReturnNext(
                fragment -> {
                    operatorParts.add(fragment);
                    return !isOperator(operatorParts);
                },
                fragment -> {
                },
                nextFragmentSupplier);
        final var nextFragment = operatorParts.removeLast();
        clause.setOperator(operatorFullLexemes.get(String.join(LEX_SPACE, operatorParts)));
        return nextFragment;
    }

    private boolean isOperator(List<String> parts) {
        return operatorFullLexemes.containsKey(String.join(LEX_SPACE, parts));
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
