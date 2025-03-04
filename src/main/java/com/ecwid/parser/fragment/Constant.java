package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Aliasable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Constant implements Aliasable {
    private final String value;
    private String alias;

    @Override
    public String toString() {
        return print(value);
    }

    @Override
    @JsonIgnore(value = false)
    public String getValue() {
        return value;
    }
}
