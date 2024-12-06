package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.NameAliasPair;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_CLOSE_BRACKET;
import static com.ecwid.parser.Lexemes.LEX_OPEN_BRACKET;

public interface CanHaveFunction extends Crawler {

    default String getFunctionSignature(NameAliasPair pair, Supplier<String> fragments) {
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
}
