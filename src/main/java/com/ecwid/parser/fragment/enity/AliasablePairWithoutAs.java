package com.ecwid.parser.fragment.enity;

import lombok.AccessLevel;
import lombok.Setter;

import static com.ecwid.parser.Lexemes.LEX_AS;

public class AliasablePairWithoutAs implements Aliasable {

    @Setter(AccessLevel.PRIVATE)
    private String name;

    @Setter
    private String alias;

    @Override
    public String name() {
        return name;
    }

    @Override
    public String alias() {
        return alias;
    }

    public void setPart(String part) {
        if (LEX_AS.equals(part)) {
            return;
        }
        if (name == null) {
            setName(part);
        } else {
            setAlias(part);
        }
    }
}
