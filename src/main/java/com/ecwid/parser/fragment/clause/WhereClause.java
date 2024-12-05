package com.ecwid.parser.fragment.clause;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ecwid.parser.Lexemes.*;

@Data
@RequiredArgsConstructor
public class WhereClause {

    private Operand leftOperand;
    private Operand rightOperand;
    private Operator operator;
    private final ClauseType clauseType;

    public void setNextOperand(Operand operand) {
        if (leftOperand == null) {
            leftOperand = operand;
        } else {
            rightOperand = operand;
        }
    }

    @Getter
    public enum Operator {
        EQUALS(LEX_EQUALS),
        NOT_EQUALS(LEX_NOT_EQUALS),
        GREATER_THAN(LEX_GREATER_THAN),
        LESS_THAN(LEX_LESS_THAN),
        GREATER_THAN_OR_EQUALS(LEX_GREATER_THAN_OR_EQUALS),
        LESS_THAN_OR_EQUALS(LEX_LESS_THAN_OR_EQUALS),
        LIKE(LEX_LIKE),
        NOT_LIKE(LEX_NOT + LEX_LIKE),
        IN(LEX_IN),
        NOT_IN(LEX_NOT + LEX_IN),
        IS(LEX_IS),
        IS_NOT(LEX_IS + LEX_NOT);

        private final String fullLexeme;

        Operator(String fullLexeme) {
            this.fullLexeme = fullLexeme;
        }

        public static final Map<String, Operator> operatorFullLexemes = Arrays.stream(WhereClause.Operator.values())
                .collect(Collectors.toMap(WhereClause.Operator::getFullLexeme, Function.identity()));

    }

    public enum ClauseType {
        AND,
        OR,
        WHERE,
        HAVING
    }
}
