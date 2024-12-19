package com.ecwid.parser.crawler.base;

import lombok.Getter;

import static com.ecwid.parser.Lexemes.LEX_AS;

@Getter
class NameAliasPair {

    private String name;

    private String alias;

    public void push(String part) {
        if (name == null) {
            name = part;
        } else {
            alias = part;
        }
    }

    public void reset() {
        name = null;
        alias = null;
    }
}
