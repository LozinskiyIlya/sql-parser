package com.ecwid.parser.fragment.enity;

import lombok.AccessLevel;
import lombok.Setter;

import static com.ecwid.parser.Lexemes.LEX_AS;

public class AliasCleaner implements Nameable {

    @Setter(AccessLevel.PRIVATE)
    private String name;

    @Setter
    private String alias;

    public String name() {
        return name;
    }

    @Override
    public String alias() {
        return alias;
    }

    public void push(String part) {
        if (LEX_AS.equals(part)) {
            return;
        }
        if (name == null) {
            setName(part);
        } else {
            setAlias(part);
        }
    }

    public void reset() {
        setName(null);
        setAlias(null);
    }
}
