package com.ecwid.parser.fragment.enity;

import com.ecwid.parser.fragment.source.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
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
