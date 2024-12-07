package com.ecwid.parser.fragment.domain;


public interface Aliasable extends Fragment {
    String getAlias();

    void setAlias(String alias);

    default String print(String rest) {
        return getAlias() == null ? rest : String.format("%s %s", rest, getAlias());
    }
}
