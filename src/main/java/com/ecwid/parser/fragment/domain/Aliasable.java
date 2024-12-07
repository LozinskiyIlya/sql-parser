package com.ecwid.parser.fragment.domain;


public interface Aliasable extends Fragment {
    String getAlias();

    default String print(String rest) {
        return getAlias() == null ? rest : String.format("%s %s", rest, getAlias());
    }
}
