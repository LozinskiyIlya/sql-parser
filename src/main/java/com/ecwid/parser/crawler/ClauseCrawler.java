package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.clause.*;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Component
public class ClauseCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String clauseName, Supplier<String> fragmentSupplier) {
        final var clause = new WhereClause(WhereClause.ClauseType.valueOf(clauseName.toUpperCase()));
        final var operator = setOperandAndReturnNextFragment(clause, fragmentSupplier, null);
        final var nexFragment = setOperandAndReturnNextFragment(clause, fragmentSupplier, operator);
        clause.setOperator(operator);
        query.getWhereClauses().add(clause);
        delegate(query, nexFragment, fragmentSupplier);
    }


    private String setOperandAndReturnNextFragment(WhereClause clause, Supplier<String> fragmentSupplier, String operator) {
        Operand operand;
        var fragment = fragmentSupplier.get();
        if (LEX_OPEN_BRACKET.equals(fragment)) {
            fragment = fragmentSupplier.get();
        }
        if (LEX_SELECT.equals(fragment)) {
            final var nestedQuery = new Query();
            operand = new QueryOperand(nestedQuery);
            selectCrawler(fragment).crawl(nestedQuery, fragment, fragmentSupplier);
            fragment = fragmentSupplier.get();
        } else if (LEX_IN.equals(operator)) {
            operand = new ListOperand();
            final var values = ((ListOperand) operand).getValues();
            values.add(fragment);
            crawlUntil(fragmentSupplier, this::shouldDelegate, values::add);
        } else if (isConstant(fragment)) {
            operand = new ConstantOperand(fragment);
            clause.setNextOperand(operand);
            fragment = fragmentSupplier.get();
        } else {
            operand = new ColumnOperand(fragment);
            fragment = fragmentSupplier.get();
        }
        clause.setNextOperand(operand);
        return fragment;
    }

    private void crawlUntil(Supplier<String> fragmentSupplier, Predicate<String> until, Consumer<String> action) {
        String fragment;
        while ((fragment = fragmentSupplier.get()) != null) {
            if (until.test(fragment)) {
                break;
            }
            action.accept(fragment);
        }
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
