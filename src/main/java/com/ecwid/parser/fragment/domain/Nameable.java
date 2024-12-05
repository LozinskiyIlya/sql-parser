package com.ecwid.parser.fragment.domain;

public interface Nameable extends Aliasable {
    String name();

    default String print() {
        return "%s %s".formatted(name(), alias());
    }
}
