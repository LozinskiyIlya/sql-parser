package com.ecwid.parser.fragment.clause;

import lombok.Data;

import java.util.List;

@Data
public class ListOperand implements Operand {
    private List<String> values;
}
