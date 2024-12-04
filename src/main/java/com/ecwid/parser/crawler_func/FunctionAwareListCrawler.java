package com.ecwid.parser.crawler_func;

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
                flushItem().accept(query, acc.toString());
                return;
            }
            final var currentString = acc.toString();
            if (currentString.contains(LEX_OPEN_BRACKET)) {
                if (currentString.contains(LEX_CLOSE_BRACKET)) {
                    flushItem().accept(query, acc.toString());
                    flushItem().accept(query, fragment);
                    return;
                }
                acc.append(fragment);
                flushItem().accept(query, acc.toString());
                return;
            }
            if (!acc.isEmpty()) {
                flushItem().accept(query, acc.toString());
            }
            flushItem().accept(query, fragment);
        };
    }
}