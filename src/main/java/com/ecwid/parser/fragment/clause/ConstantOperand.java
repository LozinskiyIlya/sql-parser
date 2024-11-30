package com.ecwid.parser.fragment.clause;

import lombok.Data;

@Data
public class ConstantOperand implements Operand {
    private String value;
}
