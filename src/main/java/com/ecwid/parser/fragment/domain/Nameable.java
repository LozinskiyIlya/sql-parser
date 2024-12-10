package com.ecwid.parser.fragment.domain;

public interface Nameable extends Aliasable {
    String getName();

    void setName(String name);

    default String print() {
        return Aliasable.super.print(getName());
    }

    @Override
    default String getValue() {
        return getName();
    }
}
