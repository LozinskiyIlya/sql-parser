package com.ecwid.parser.fragment.clause;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ConstantListOperand implements Operand {
    private List<String> values = new LinkedList<>();

    @Override
    public String toString() {
        return String.join(", ", values);
    }
}
