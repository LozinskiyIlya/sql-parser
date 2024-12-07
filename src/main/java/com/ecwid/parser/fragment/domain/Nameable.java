package com.ecwid.parser.fragment.domain;

public interface Nameable extends Aliasable {
    String getName();

    default String print() {
        return Aliasable.super.print(getName());
    }
}
