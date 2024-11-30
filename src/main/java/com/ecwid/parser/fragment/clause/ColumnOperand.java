package com.ecwid.parser.fragment.clause;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnOperand implements Operand {
    private String column;
}
