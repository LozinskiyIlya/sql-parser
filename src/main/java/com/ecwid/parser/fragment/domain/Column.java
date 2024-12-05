package com.ecwid.parser.fragment.domain;

import com.ecwid.parser.fragment.clause.Operand;

public record Column(String name, String alias) implements Operand, Nameable {

    @Override
    public String toString() {
        return this.print();
    }
}
