package com.ecwid.parser.fragment.clause;

import com.ecwid.parser.fragment.domain.Constructable;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static com.ecwid.parser.Lexemes.*;

@Data
@RequiredArgsConstructor
public class Condition {

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
    public enum Operator implements Constructable {
        EQUALS(LEX_EQUALS),
        NOT_EQUALS(LEX_NOT + LEX_EQUALS),
        GREATER_THAN(LEX_GREATER_THAN),
        LESS_THAN(LEX_LESS_THAN),
        GREATER_THAN_OR_EQUALS(LEX_GREATER_THAN_OR_EQUALS),
        LESS_THAN_OR_EQUALS(LEX_LESS_THAN_OR_EQUALS),
        LIKE(LEX_LIKE),
        NOT_LIKE(LEX_NOT + LEX_SPACE + LEX_LIKE),
        IN(LEX_IN),
        NOT_IN(LEX_NOT + LEX_SPACE + LEX_IN),
        IS(LEX_IS),
        IS_NOT(LEX_IS + LEX_SPACE + LEX_NOT);


        private final String fullLexeme;

        Operator(String fullLexeme) {
            this.fullLexeme = fullLexeme;
        }

        public static final Map<String, Operator> operatorFullLexemes = Constructable.createLexemeMap(Operator.class);

    }

    public enum ClauseType {
        AND,
        OR,
        WHERE,
        HAVING
    }
}
