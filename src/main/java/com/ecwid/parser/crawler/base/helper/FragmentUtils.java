package com.ecwid.parser.crawler.base.helper;

import org.springframework.util.StringUtils;

import java.util.List;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.fragment.Condition.Operator.operatorFullLexemes;

public class FragmentUtils {
    public static boolean isOperator(List<String> parts) {
        return operatorFullLexemes.containsKey(String.join(LEX_SPACE, parts));
    }

    public static boolean isConstant(String fragment) {
        return isQuotedString(fragment) || isConstantNumber(fragment) || isNullConstant(fragment);
    }

    public static boolean nameNotSet(NameAliasPair pair) {
        return !StringUtils.hasText(pair.getName());
    }

    private static boolean isQuotedString(String fragment) {
        return fragment.startsWith(LEX_SINGLE_QUOTE) && fragment.endsWith(LEX_SINGLE_QUOTE);
    }

    private static boolean isNullConstant(String fragment) {
        return LEX_NULL.equals(fragment);
    }

    private static boolean isConstantNumber(String fragment) {
        return fragment.matches("^-?\\d+(\\.\\d+)?$");
    }
}
