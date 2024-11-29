package com.ecwid.parser.fragment.clause;

import lombok.Data;

@Data
public class ColumnOperand implements Operand {
    private String column;
}
