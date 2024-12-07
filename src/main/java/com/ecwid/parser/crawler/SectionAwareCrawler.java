package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.NameAliasPair;
import com.ecwid.parser.fragment.domain.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_CLOSE_BRACKET;
import static com.ecwid.parser.Lexemes.LEX_OPEN_BRACKET;

abstract class SectionAwareCrawler implements Crawler {

    @Lazy
    @Autowired
    private Map<String, Crawler> sectionAgainstCrawler;

    protected final String getFunctionSignature(NameAliasPair pair, Supplier<String> fragments) {
        final var functionBuilder = new StringBuilder();
        if (StringUtils.hasText(pair.getFirst())) {
            // the last inserted value was a function name
            functionBuilder.append(pair.getFirst());
            pair.reset();
        }
        functionBuilder.append(LEX_OPEN_BRACKET);
        crawlUntilAndReturnNext(LEX_CLOSE_BRACKET::equals, functionBuilder::append, fragments);
        functionBuilder.append(LEX_CLOSE_BRACKET);
        return functionBuilder.toString();
    }

    protected final boolean shouldDelegate(String nextFragment) {
        return sectionAgainstCrawler.containsKey(nextFragment);
    }

    @Override
    public final void delegate(Query query, String currentSection, Supplier<String> fragments) {
        Crawler.super.delegate(query, currentSection, fragments);
    }

    @Override
    public final Optional<Crawler> nextCrawler(String currentSection) {
        return Optional.ofNullable(sectionAgainstCrawler.get(currentSection));
    }

    @Override
    public final String crawlUntilAndReturnNext(Predicate<String> fragmentIs, Consumer<String> andDoAction, Supplier<String> fragments) {
        return Crawler.super.crawlUntilAndReturnNext(fragmentIs, andDoAction, fragments);
    }
}
