package com.ecwid.parser.clause;

import lombok.Data;

@Data
public class WhereClause {

    private Operand leftOperand;
    private Operand rightOperand;
    private Operator operator;
    private Delimiter delimiter;

    enum Operator {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN_OR_EQUALS,
        LIKE,
        NOT_LIKE,
        IN,
        NOT_IN,
        IS_NULL,
        IS_NOT_NULL
    }

    enum Delimiter {
        AND,
        OR
    }
}
