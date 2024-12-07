package com.ecwid.parser.fragment.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface MultiLex {
    String getFullLexeme();

    static <T extends Enum<T> & MultiLex> Map<String, T> createLexemeMap(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(MultiLex::getFullLexeme, Function.identity()));
    }
}
