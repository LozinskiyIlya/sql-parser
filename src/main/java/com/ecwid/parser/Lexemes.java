package com.ecwid.parser;

import java.util.Set;

public class Lexemes {

    public static final String LEX_SELECT = "select";
    public static final String LEX_ASTER = "*";
    public static final String LEX_AS = "as";
    public static final String LEX_FROM = "from";
    public static final String LEX_JOIN = "join";
    public static final String LEX_LEFT = "left";
    public static final String LEX_RIGHT = "right";
    public static final String LEX_FULL = "full";
    public static final String LEX_ON = "on";
    public static final String LEX_WHERE = "where";
    public static final String LEX_HAVING = "having";
    public static final String LEX_AND = "and";
    public static final String LEX_OR = "or";
    public static final String LEX_NOT = "not";
    public static final String LEX_IS = "is";
    public static final String LEX_IN = "in";
    public static final String LEX_LIKE = "like";
    public static final String LEX_GROUP = "group";
    public static final String LEX_ORDER = "order";
    public static final String LEX_BY = "by";
    public static final String LEX_ASC = "asc";
    public static final String LEX_DESC = "desc";
    public static final String LEX_NULLS = "nulls";
    public static final String LEX_LAST = "last";
    public static final String LEX_FIRST = "first";
    public static final String LEX_LIMIT = "limit";
    public static final String LEX_OFFSET = "offset";
    public static final String LEX_COUNT = "count";
    public static final String LEX_SUM = "sum";
    public static final String LEX_AVG = "avg";
    public static final String LEX_MIN = "min";
    public static final String LEX_MAX = "max";
    public static final String LEX_NULL = "null";
    public static final String LEX_EQUALS = "=";
    public static final String LEX_GREATER_THAN = ">";
    public static final String LEX_LESS_THAN = "<";
    public static final String LEX_GREATER_THAN_OR_EQUALS = ">=";
    public static final String LEX_LESS_THAN_OR_EQUALS = "<=";
    public static final String LEX_DOT = ".";
    public static final String LEX_COMMA = ",";
    public static final String LEX_SEMICOLON = ";";
    public static final String LEX_LEFT_BRACKET = "(";
    public static final String LEX_RIGHT_BRACKET = ")";


    public static final Set<String> COMMANDS = Set.of(
            LEX_SELECT,
            LEX_FROM,
            LEX_JOIN,
            LEX_LEFT,
            LEX_RIGHT,
            LEX_FULL,
            LEX_WHERE,
            LEX_HAVING,
            LEX_GROUP,
            LEX_ORDER,
            LEX_LIMIT,
            LEX_OFFSET
    );

    public static final Set<String> JOINS = Set.of(
            LEX_LEFT,
            LEX_RIGHT,
            LEX_FULL
    );

    public static final Set<String> SEPARATORS = Set.of(
            LEX_COMMA,
            LEX_SEMICOLON,
            LEX_LEFT_BRACKET,
            LEX_RIGHT_BRACKET
    );

}
