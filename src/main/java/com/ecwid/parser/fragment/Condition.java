package com.ecwid.parser.fragment;

import com.ecwid.parser.QueryPrinter;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.MultiLex;
import lombok.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static com.ecwid.parser.Lexemes.*;

@Getter
@RequiredArgsConstructor
public class Condition {

    @Getter(AccessLevel.NONE)
    private BuildStep buildStep = BuildStep.EXPECTING_LEFT;

    private Fragment leftOperand;

    private Fragment rightOperand;

    private Operator operator;

    private final ClauseType clauseType;

    public void addNextPart(Fragment part) {
        switch (buildStep) {
            case EXPECTING_LEFT:
                leftOperand = part;
                buildStep = BuildStep.EXPECTING_OPERATOR;
                break;
            case EXPECTING_OPERATOR:
                operator = (Operator) part;
                buildStep = BuildStep.EXPECTING_RIGHT;
                break;
            case EXPECTING_RIGHT:
                rightOperand = part;
                buildStep = BuildStep.DONE;
                break;
            default:
                throw new IllegalStateException("Unexpected fragment: " + part);
        }
    }

    @Override
    public String toString() {
        return QueryPrinter.printCondition(this);
    }

    @Getter
    public enum Operator implements MultiLex, Fragment {
        EQUALS(LEX_EQUALS),
        NOT_EQUALS(LEX_NOT_EQUALS),
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

        @Override
        public Operator getValue() {
            return this;
        }
    }

    public enum ClauseType {
        ON,
        AND,
        OR,
        WHERE,
        HAVING;

        public static Optional<ClauseType> fromString(String name) {
            return Arrays.stream(values())
                    .filter(type -> type.name().equalsIgnoreCase(name))
                    .findFirst();
        }
    }

    private enum BuildStep {
        EXPECTING_LEFT,
        EXPECTING_OPERATOR,
        EXPECTING_RIGHT,
        DONE
    }
}
