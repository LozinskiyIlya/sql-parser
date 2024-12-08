package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Nameable;
import com.ecwid.parser.fragment.domain.Source;
import lombok.Data;


@Data
public class Table implements Source, Nameable {
    private String alias;
    private String name;

    @Override
    public String toString() {
        return print();
    }
}
