package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Fragment;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class ConstantListOperand implements Fragment {
    private List<String> values = new LinkedList<>();

    @Override
    public String toString() {
        return String.join(", ", values);
    }
}
