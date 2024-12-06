package com.ecwid.parser.fragment.domain;

public interface Nameable extends Aliasable {
    String name();

    default String print() {
        return alias() == null ? name() : String.format("%s %s", name(), alias());
    }
}
