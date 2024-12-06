package com.ecwid.parser;


import java.util.Set;

public class Lexemes {

    public static final String LEX_SELECT = "select";
    public static final String LEX_ASTER = "*";
    public static final String LEX_AS = "as";
    public static final String LEX_FROM = "from";
    public static final String LEX_JOIN = "join";
    public static final String LEX_INNER = "inner";
    public static final String LEX_OUTER = "outer";
    public static final String LEX_CROSS = "cross";
    public static final String LEX_NATURAL = "natural";
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
    public static final String LEX_NOT_EQUALS = "!=";
    public static final String LEX_GREATER_THAN = ">";
    public static final String LEX_LESS_THAN = "<";
    public static final String LEX_GREATER_THAN_OR_EQUALS = ">=";
    public static final String LEX_LESS_THAN_OR_EQUALS = "<=";
    public static final String LEX_DOT = ".";
    public static final String LEX_COMMA = ",";
    public static final String LEX_SEMICOLON = ";";
    public static final String LEX_OPEN_BRACKET = "(";
    public static final String LEX_CLOSE_BRACKET = ")";
    public static final String LEX_SINGLE_QUOTE = "'";
    public static final String LEX_SPACE = " ";

    public static final Set<String> JOIN_TYPES = Set.of(
            LEX_LEFT,
            LEX_RIGHT,
            LEX_FULL,
            LEX_INNER,
            LEX_OUTER,
            LEX_CROSS,
            LEX_NATURAL
    );

    public static final Set<String> CONDITION_SEPARATORS = Set.of(
            LEX_AND,
            LEX_OR
    );

    public static final Set<String> SEPARATORS = Set.of(
            LEX_OPEN_BRACKET,
            LEX_CLOSE_BRACKET,
            LEX_SINGLE_QUOTE,
            LEX_COMMA,
            LEX_SEMICOLON
    );
}
