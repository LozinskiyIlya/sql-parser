package com.ecwid.parser.fragment.domain;

public interface Nameable extends Aliasable {
    String getName();

    void setName(String name);

    @Override
    default String getValue() {
        return getName();
    }

    default String print() {
        return Aliasable.super.print(getName());
    }
}
