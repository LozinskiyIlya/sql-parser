package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Query;

import java.util.Optional;
import java.util.function.BiConsumer;

import static com.ecwid.parser.Lexemes.LEX_COMMA;

public interface ListCrawler {

    BiConsumer<Query, String> onListItem();

    default BiConsumer<Query, String> addToQuery() {
        return (query, fragment) -> Optional.of(fragment)
                .filter(f -> !LEX_COMMA.equals(f))
                .ifPresent(f -> onListItem().accept(query, f));
    }
}