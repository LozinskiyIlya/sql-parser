package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Nameable;
import com.ecwid.parser.fragment.domain.Source;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class Table implements Source, Nameable {
    private String alias;
    private String name;

    public Table(NameAliasPair pair) {
        this.alias = pair.getSecond();
        this.name = pair.getFirst();
    }

    @Override
    public String toString() {
        return print();
    }
}
