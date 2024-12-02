package com.ecwid.parser.fragment.enity;

import com.ecwid.parser.fragment.source.Source;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Table implements Source {
    private String name;
    private String alias;
}
