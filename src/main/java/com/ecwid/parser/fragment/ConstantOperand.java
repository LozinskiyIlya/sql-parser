package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Aliasable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ConstantOperand implements Aliasable {
    private final String value;
    private String alias;

    @Override
    public String toString() {
        return value;
    }
}
