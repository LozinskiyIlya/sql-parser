package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Query;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.ecwid.parser.Lexemes.LEX_CLOSE_BRACKET;
import static com.ecwid.parser.Lexemes.LEX_OPEN_BRACKET;

public interface FunctionAwareListCrawler extends ListCrawler {

    BiConsumer<Query, String> flushItem();

    Function<Query, StringBuilder> acc();

    @Override
    default BiConsumer<Query, String> onListItem() {
        return (query, fragment) -> {
            final var acc = acc().apply(query);
            if (LEX_OPEN_BRACKET.equals(fragment)) {
                acc.append(fragment);
                return;
            }
            final var currentString = acc.toString();
            if (currentString.contains(LEX_OPEN_BRACKET) && !currentString.contains(LEX_CLOSE_BRACKET)) {
                acc.append(fragment);
                return;
            }
            if (LEX_CLOSE_BRACKET.equals(fragment)) {
                acc.append(fragment);
                flushItem().accept(query, acc.toString());
                return;
            }
            flushItem().accept(query, fragment);
        };
    }
}