package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.*;
import com.ecwid.parser.fragment.domain.Aliasable;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Nameable;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@RequiredArgsConstructor
public abstract class FragmentCrawler extends SectionAwareCrawler {

    protected final BiConsumer<Query, Fragment> onFragment;

    protected abstract String crawlClauseAndReturnNextLex(Query query, String currentSection, Supplier<String> nextLex);

    @Override
    public final void crawl(Query query, String currentSection, Supplier<String> nextLex) {
        Fragment fragment = null;
        var lex = crawlClauseAndReturnNextLex(query, currentSection, nextLex);
        final var pair = new NameAliasPair();
        do {
            if (LEX_COMMA.equals(lex)) {
                flush(query, fragment, pair);
                continue;
            }
            if (LEX_CLOSE_BRACKET.equals(lex)) {
                break;
            }
            if (LEX_OPEN_BRACKET.equals(lex)) {
                // either a function argument or a sub query select or a list of values
                lex = nextLex.get();
                if (LEX_SELECT.equals(lex)) {
                    fragment = new Query();
                    nextCrawler(lex).orElseThrow().crawl((Query) fragment, lex, nextLex);
                } else {
                    fragment = new Column();
                    lex = getFunctionSignature(pair.getFirst(), lex, nextLex);
                    pair.reset();
                }
            } else if (!StringUtils.hasText(pair.getFirst())) {
                if (isConstant(lex)) {
                    fragment = new Constant(lex);
                } else if (this instanceof ParsingSource) {
                    fragment = new Table();
                } else {
                    fragment = new Column();
                }
            }

            pair.push(lex);

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
        onFragment.accept(query, fragment);
        pair.reset();
    }

    private String getFunctionSignature(String functionName, String firstArg, Supplier<String> fragments) {
        final var functionBuilder = new StringBuilder(functionName);
        functionBuilder.append(LEX_OPEN_BRACKET);
        functionBuilder.append(firstArg);
        crawlUntilAndReturnNext(LEX_CLOSE_BRACKET::equals, functionBuilder::append, fragments);
        functionBuilder.append(LEX_CLOSE_BRACKET);
        return functionBuilder.toString();
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
