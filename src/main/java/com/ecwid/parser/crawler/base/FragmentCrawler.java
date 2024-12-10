package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.*;
import com.ecwid.parser.fragment.Condition.Operator;
import com.ecwid.parser.fragment.domain.Aliasable;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Nameable;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.fragment.Condition.Operator.operatorFullLexemes;

public abstract class FragmentCrawler extends SectionAwareCrawler {

    protected abstract String processClauseAndReturnNextLex(Query query, String currentSection, Supplier<String> nextLex);

    protected abstract void processFragment(Query query, Fragment fragment);

    protected boolean crawlsForSources() {
        return false;
    }

    @Override
    public final void crawl(Query query, String currentSection, Supplier<String> nextLex, int openBrackets) {
        Fragment fragment = null;
        int brackets = openBrackets;
        var lex = processClauseAndReturnNextLex(query, currentSection, nextLex);
        final var pair = new NameAliasPair();
        do {
            if (SKIP_LEX.contains(lex)) {
                continue;
            }
            if (LEX_COMMA.equals(lex)) {
                flush(query, fragment, pair);
                continue;
            }
            if (LEX_CLOSE_BRACKET.equals(lex)) {
                if (--brackets == 0) {
                    break;
                }
                continue;
            }

            if (OPERATORS.contains(lex)) {
                flush(query, fragment, pair); // flush left operand
                final var operator = new AtomicReference<Operator>();
                lex = crawlForOperator(operator, lex, nextLex); // first lexeme of right operand
                fragment = operator.get();
                flush(query, fragment, pair); // flush operator
            }

            if (LEX_OPEN_BRACKET.equals(lex)) {
                lex = nextLex.get();
                if (LEX_SELECT.equals(lex)) {
                    // nested query
                    fragment = new Query();
                    nextCrawler(lex).orElseThrow().crawl((Query) fragment, lex, nextLex, 1);
                } else if (isConstant(lex)) {
                    // constant list
                    fragment = new ConstantList();
                    lex = crawlForList((ConstantList) fragment, lex, nextLex);
                } else {
                    // condition in brackets
                    this.crawl(query, lex, nextLex, 1);
                }
            } else if (!StringUtils.hasText(pair.getFirst())) {
                if (isConstant(lex)) {
                    fragment = new Constant(lex);
                } else if (crawlsForSources()) {
                    fragment = new Table();
                } else {
                    fragment = new Column();
                }
            }

            pair.push(lex);
        } while (!shouldDelegate(lex = nextLex.get()));

        flush(query, fragment, pair);
        delegate(query, lex, nextLex, brackets);
    }

    private void flush(Query query, Fragment fragment, NameAliasPair pair) {
        if (fragment == null) {
            return;
        }
        if (fragment instanceof Nameable) {
            ((Nameable) fragment).setName(pair.getFirst());
        }
        if (fragment instanceof Aliasable) {
            ((Aliasable) fragment).setAlias(pair.getSecond());
        }
        processFragment(query, fragment);
        pair.reset();
    }

    private String crawlForList(ConstantList list, String firstItem, Supplier<String> nextLex) {
        final var values = list.getValues();
        values.add(firstItem);
        return crawlUntilAndReturnNext(
                LEX_CLOSE_BRACKET::equals,
                lex -> {
                    if (LEX_COMMA.equals(lex)) {
                        return;
                    }
                    values.add(lex);
                },
                nextLex);
    }

    private String crawlForOperator(AtomicReference<Operator> operator, String operatorFirstLex, Supplier<String> nextLex) {
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
        operator.set(operatorFullLexemes.get(String.join(LEX_SPACE, operatorParts)));
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
        return fragment.matches("^-?\\d+(\\.\\d+)?$");
    }
}
