package com.ecwid.parser.crawler_func;

import com.ecwid.parser.fragment.enity.Query;

import java.util.Optional;
import java.util.function.BiConsumer;

import static com.ecwid.parser.Lexemes.LEX_COMMA;
import static java.util.function.Predicate.not;

public interface ListCrawler {

    BiConsumer<Query, String> onListItem();

    default BiConsumer<Query, String> addToQuery() {
        return (query, fragment) -> Optional.of(fragment)
                .filter(not(LEX_COMMA::equals))
                .ifPresent(it -> onListItem().accept(query, it));
    }
}