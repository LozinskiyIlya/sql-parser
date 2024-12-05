package com.ecwid.parser.fragment.domain;

import com.ecwid.parser.fragment.source.Source;
import lombok.Setter;


@Setter
public class Table implements Source, Nameable {

    private String alias;
    private String name;

    @Override
    public String alias() {
        return alias;
    }

    @Override
    public String name() {
        return name;
    }
}
