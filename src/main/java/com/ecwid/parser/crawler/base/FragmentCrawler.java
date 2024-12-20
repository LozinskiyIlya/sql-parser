package com.ecwid.parser.crawler.base;

import com.ecwid.parser.crawler.base.helper.CrawlContext;
import com.ecwid.parser.crawler.base.helper.NameAliasPair;
import com.ecwid.parser.fragment.*;
import com.ecwid.parser.fragment.domain.Aliasable;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Nameable;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.crawler.base.helper.FragmentUtils.*;
import static com.ecwid.parser.fragment.Condition.Operator.operatorFullLexemes;

public abstract class FragmentCrawler extends SectionAwareCrawler {

    protected abstract void onClause(CrawlContext context);

    protected abstract void onFragment(Query query, Fragment fragment);

    protected boolean crawlsForSources() {
        return false;
    }

    @Override
    public final void crawl(CrawlContext context) {
        Fragment fragment = null;
        onClause(context);
        var lex = context.getCurrent();
        final var query = context.getQuery();
        final var nextLex = context.getNext();
        final var pair = new NameAliasPair();
        do {
            if (SKIP_LEX.contains(lex)) {
                if (bracketsClosed(lex, context)) {
                    break;
                }
                if (LEX_COMMA.equals(lex)) {
                    flush(query, fragment, pair);
                }
                continue;
            }

            if (OPERATORS.contains(lex)) {
                // flush left operand
                flush(query, fragment, pair);
                // first lexeme of the right operand
                lex = crawlForOperator(context.moveTo(lex), pair);
            }

            if (LEX_OPEN_BRACKET.equals(lex)) {
                fragment = crawlForNestedFragment(context, fragment);
                lex = context.getCurrent();
            } else if (nameNotSet(pair)) {
                fragment = crawlForFragment(lex);
            }

            pair.push(lex);
        } while (!shouldDelegate(lex = nextLex.get()));

        flush(query, fragment, pair);
        delegate(context.moveTo(lex));
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

    private Fragment crawlForFragment(String curLex) {
        if (isConstant(curLex)) {
            return new Constant(curLex);
        } else if (crawlsForSources()) {
            return new Table();
        } else {
            return new Column();
        }
    }

    private Fragment crawlForNestedFragment(CrawlContext context, Fragment currentFragment) {
        Fragment fragment;
        context.move();
        final var curLex = context.getCurrent();
        final var nextLex = context.getNext();
        final var query = context.getQuery();
        if (LEX_SELECT.equals(curLex) || crawlsForSources()) {
            // nested query or join
            fragment = new Query();
            nextCrawler(curLex).orElseThrow().crawl(new CrawlContext((Query) fragment, curLex, nextLex, 1));
        } else if (isConstant(curLex)) {
            // constant list
            fragment = new ConstantList();
            context.moveTo(crawlForList((ConstantList) fragment, curLex, nextLex));
        } else {
            // nested condition
            this.crawl(new CrawlContext(query, curLex, nextLex, 1));
            fragment = currentFragment;
        }
        return fragment;
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

    private String crawlForOperator(CrawlContext context, NameAliasPair pair) {
        final var operatorParts = new LinkedList<String>();
        operatorParts.add(context.getCurrent());
        crawlUntilAndReturnNext(
                nextLex -> {
                    operatorParts.add(nextLex);
                    return !isOperator(operatorParts);
                },
                nextLex -> {
                },
                context.getNext());
        final var nextLex = operatorParts.removeLast();
        final var operator = operatorFullLexemes.get(String.join(LEX_SPACE, operatorParts));
        flush(context.getQuery(), operator, pair);
        return nextLex;
    }
}