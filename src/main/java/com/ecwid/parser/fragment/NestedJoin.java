package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Source;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NestedJoin extends Join implements Source {
    private Source source;
    private String alias;

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String getValue() {
        return this.toString();
    }
}
