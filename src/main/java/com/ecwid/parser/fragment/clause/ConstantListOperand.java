package com.ecwid.parser.fragment.clause;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class ConstantListOperand implements Operand {
    private List<String> values = new LinkedList<>();
}