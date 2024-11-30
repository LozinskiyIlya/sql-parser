package com.ecwid.parser.fragment.clause;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConstantOperand implements Operand {
    private String value;
}
