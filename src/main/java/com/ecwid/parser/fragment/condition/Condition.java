package com.ecwid.parser.fragment.condition;

import com.ecwid.parser.fragment.domain.MultiLex;
import com.ecwid.parser.fragment.domain.Query;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
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

    @Override
    public String toString() {
        final var builder = new LinkedList<String>();
        builder.add(clauseType.name());
        printOperand(builder, leftOperand);
        builder.add(operator.getFullLexeme());
        printOperand(builder, rightOperand);
        return String.join(LEX_SPACE, builder);
    }

    private static void printOperand(List<String> builder, Operand operand) {
        if (operand instanceof Query || operand instanceof ConstantListOperand) {
            builder.add(LEX_OPEN_BRACKET);
            builder.add(operand.toString());
            builder.add(LEX_CLOSE_BRACKET);
        } else {
            builder.add(operand.toString());
        }
    }

    @Getter
    public enum Operator implements MultiLex {
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

        public static final Map<String, Operator> operatorFullLexemes = MultiLex.createLexemeMap(Operator.class);

    }

    public enum ClauseType {
        ON,
        AND,
        OR,
        WHERE,
        HAVING
    }
}
