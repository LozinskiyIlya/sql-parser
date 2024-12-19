package com.ecwid.parser.crawler.base;

import com.ecwid.parser.crawler.base.helper.CrawlContext;
import com.ecwid.parser.crawler.base.helper.NameAliasPair;
import com.ecwid.parser.fragment.*;
import com.ecwid.parser.fragment.Condition.Operator;
import com.ecwid.parser.fragment.domain.Aliasable;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Nameable;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.crawler.base.helper.FragmentUtils.isConstant;
import static com.ecwid.parser.crawler.base.helper.FragmentUtils.isOperator;
import static com.ecwid.parser.fragment.Condition.Operator.operatorFullLexemes;

public abstract class FragmentCrawler extends SectionAwareCrawler {

    protected abstract String lexAfterClause(CrawlContext context);

    protected abstract void onFragment(Query query, Fragment fragment);

    @Override
    public final void crawl(CrawlContext context) {
        Fragment fragment = null;
        var lex = lexAfterClause(context);
        int brackets = context.getOpenBrackets();
        final var query = context.getQuery();
        final var nextLex = context.getNextLexSupplier();
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
                if (LEX_SELECT.equals(lex) || crawlsForSources()) {
                    // nested query or join
                    fragment = new Query();
                    nextCrawler(lex).orElseThrow().crawl(new CrawlContext((Query) fragment, lex, nextLex, 1));
                } else if (isConstant(lex)) {
                    // constant list
                    fragment = new ConstantList();
                    lex = crawlForList((ConstantList) fragment, lex, nextLex);
                } else {
                    // nested condition
                    this.crawl(new CrawlContext(query, lex, nextLex, 1));
                }
            } else if (!StringUtils.hasText(pair.getName())) {
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
        delegate(new CrawlContext(query, lex, nextLex, brackets));
    }


    protected boolean crawlsForSources() {
        return false;
    }

    protected final String crawlUntilAndReturnNext(Predicate<String> lexEquals, Consumer<String> andDoAction, Supplier<String> nextLex) {
        String lex;
        while ((lex = nextLex.get()) != null && lexEquals.negate().test(lex)) {
            andDoAction.accept(lex);
        }
        return lex;
    }

    private void flush(Query query, Fragment fragment, NameAliasPair pair) {
        if (fragment == null) {
            return;
        }
        if (fragment instanceof Nameable) {
            ((Nameable) fragment).setName(pair.getName());
        }
        if (fragment instanceof Aliasable) {
            ((Aliasable) fragment).setAlias(pair.getAlias());
        }
        onFragment(query, fragment);
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

}
