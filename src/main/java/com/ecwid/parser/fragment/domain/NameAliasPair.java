package com.ecwid.parser.fragment.domain;

import lombok.Getter;

import static com.ecwid.parser.Lexemes.LEX_AS;

@Getter
public class NameAliasPair {

    private String first;

    private String second;

    public void push(String part) {
        if (LEX_AS.equals(part)) {
            return;
        }
        if (first == null) {
            first = part;
        } else {
            second = part;
        }
    }

    public void reset() {
        first = null;
        second = null;
    }
}
