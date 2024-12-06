package com.ecwid.parser.fragment.condition;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class ConstantListOperand implements Operand {
    private List<String> values = new LinkedList<>();

    @Override
    public String toString() {
        return String.join(", ", values);
    }
}
