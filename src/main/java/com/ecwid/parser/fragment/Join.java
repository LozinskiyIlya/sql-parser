package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Column;
import com.ecwid.parser.fragment.domain.Constructable;
import com.ecwid.parser.fragment.domain.Table;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

import static com.ecwid.parser.Lexemes.*;

@Data
public class Join {
    private JoinType type;
    private Table table;
    private Column leftColumn;
    private Column rightColumn;

    @Getter
    public enum JoinType implements Constructable {
        JOIN(LEX_JOIN),
        INNER(LEX_INNER + LEX_JOIN),
        LEFT(LEX_LEFT + LEX_JOIN),
        LEFT_OUTER(LEX_LEFT + LEX_OUTER + LEX_JOIN),
        RIGHT(LEX_RIGHT + LEX_JOIN),
        RIGHT_OUTER(LEX_RIGHT + LEX_OUTER + LEX_JOIN),
        FULL(LEX_FULL + LEX_JOIN),
        FULL_OUTER(LEX_FULL + LEX_OUTER + LEX_JOIN),
        CROSS(LEX_CROSS + LEX_JOIN),
        NATURAL(LEX_NATURAL + LEX_JOIN),
        NATURAL_INNER(LEX_NATURAL + LEX_INNER + LEX_JOIN),
        NATURAL_LEFT(LEX_NATURAL + LEX_LEFT + LEX_JOIN),
        NATURAL_LEFT_OUTER(LEX_NATURAL + LEX_LEFT + LEX_OUTER + LEX_JOIN),
        NATURAL_RIGHT(LEX_NATURAL + LEX_RIGHT + LEX_JOIN),
        NATURAL_RIGHT_OUTER(LEX_NATURAL + LEX_RIGHT + LEX_OUTER + LEX_JOIN),
        NATURAL_FULL(LEX_NATURAL + LEX_FULL + LEX_JOIN),
        NATURAL_FULL_OUTER(LEX_NATURAL + LEX_FULL + LEX_OUTER + LEX_JOIN);

        private final String fullLexeme;

        JoinType(String fullLexeme) {
            this.fullLexeme = fullLexeme;
        }

        public static final Map<String, JoinType> joinTypeFullLexemes = Constructable.createLexemeMap(JoinType.class);
    }

}
