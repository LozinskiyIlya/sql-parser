package com.ecwid.parser.fragment.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Constructable {
    String getFullLexeme();

    static <T extends Enum<T> & Constructable> Map<String, T> createLexemeMap(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(Constructable::getFullLexeme, Function.identity()));
    }
}
