package com.ecwid.parser.fragment.enity;

import com.ecwid.parser.fragment.clause.Operand;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Column implements Operand, Aliasable {
    private String name;
    private String alias;
}
