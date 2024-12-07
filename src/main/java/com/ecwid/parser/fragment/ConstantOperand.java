package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Aliasable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConstantOperand implements Aliasable {
    private String value;
    private String alias;

    @Override
    public String toString() {
        return value;
    }
}
