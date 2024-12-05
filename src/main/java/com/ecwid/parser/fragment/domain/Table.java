package com.ecwid.parser.fragment.domain;

import com.ecwid.parser.fragment.source.Source;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@NoArgsConstructor
public class Table implements Source, Nameable {
    private String alias;
    private String name;

    public Table(NameAliasPair pair) {
        this.alias = pair.getSecond();
        this.name = pair.getFirst();
    }

    @Override
    public String alias() {
        return alias;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return this.print();
    }
}
