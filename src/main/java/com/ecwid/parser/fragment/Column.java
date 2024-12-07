package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Nameable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column implements Nameable {
    private String name;
    private String alias;

    @Override
    public String toString() {
        return print();
    }
}
