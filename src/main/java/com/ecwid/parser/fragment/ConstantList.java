package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Fragment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class ConstantList implements Fragment {
    private List<String> values = new LinkedList<>();

    @Override
    @JsonIgnore(value = false)
    public List<String> getValue() {
        return values;
    }

    @Override
    public String toString() {
        return String.join(", ", values);
    }
}
