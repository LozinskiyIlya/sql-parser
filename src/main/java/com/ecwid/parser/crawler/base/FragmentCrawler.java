package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.*;
import com.ecwid.parser.fragment.domain.Aliasable;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Nameable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

public abstract class FragmentCrawler extends SectionAwareCrawler {

    protected abstract String processClauseAndReturnNextLex(Query query, String currentSection, Supplier<String> nextLex);

    protected abstract void processFragment(Query query, Fragment fragment);

    protected boolean crawlsForSources() {
        return false;
    }

    protected boolean crawlOnce() {
        return false;
    }

    @Override
    public final void crawl(Query query, String currentSection, Supplier<String> nextLex) {
        Fragment fragment = null;
        var lex = processClauseAndReturnNextLex(query, currentSection, nextLex);
        final var pair = new NameAliasPair();
        if (lex == null) {
            return;
        }
        do {
            if (SKIP_LEX.contains(lex)) {
                continue;
            }
            if (LEX_COMMA.equals(lex)) {
                flush(query, fragment, pair);
                continue;
            }
            if (LEX_CLOSE_BRACKET.equals(lex)) {
                break;
            }
            if (LEX_OPEN_BRACKET.equals(lex)) {
                lex = nextLex.get();
                if (LEX_SELECT.equals(lex)) {
                    fragment = new Query();
                    nextCrawler(lex).orElseThrow().crawl((Query) fragment, lex, nextLex);
                } else if (isConstant(lex)) {
                    fragment = new ConstantList();
                    lex = crawlForList((ConstantList) fragment, lex, nextLex);
                } else {
                    fragment = new Column();
                    lex = getFunctionSignature(pair.getFirst(), lex, nextLex);
                    pair.reset();
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

            if (crawlOnce()) {
                flush(query, fragment, pair);
                return;
            }
        } while ((lex = nextLex.get()) != null && !shouldDelegate(lex));

        flush(query, fragment, pair);
        delegate(query, lex, nextLex);
    }

    private void flush(Query query, Fragment fragment, NameAliasPair pair) {
        if (fragment instanceof Nameable) {
            ((Nameable) fragment).setName(pair.getFirst());
        }
        if (fragment instanceof Aliasable) {
            ((Aliasable) fragment).setAlias(pair.getSecond());
        }
        processFragment(query, fragment);
        pair.reset();
    }

    private String getFunctionSignature(String functionName, String firstArg, Supplier<String> nextLex) {
        final var functionBuilder = new StringBuilder(functionName);
        functionBuilder.append(LEX_OPEN_BRACKET);
        functionBuilder.append(firstArg);
        crawlUntilAndReturnNext(LEX_CLOSE_BRACKET::equals, functionBuilder::append, nextLex);
        functionBuilder.append(LEX_CLOSE_BRACKET);
        return functionBuilder.toString();
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

    private static final List<String> SKIP_LEX = List.of(LEX_AS, LEX_ROWS, LEX_SEMICOLON);
}
