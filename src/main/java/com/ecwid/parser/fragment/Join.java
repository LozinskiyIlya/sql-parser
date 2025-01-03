package com.ecwid.parser.fragment;

import com.ecwid.parser.QueryPrinter;
import com.ecwid.parser.fragment.domain.MultiLex;
import com.ecwid.parser.fragment.domain.Source;
import lombok.Data;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.ecwid.parser.Lexemes.*;

@Data
public class Join {
    private JoinType joinType;
    private Source source;
    private List<Condition> conditions = new LinkedList<>();

    @Getter
    public enum JoinType implements MultiLex {
        JOIN(LEX_JOIN),
        INNER(LEX_INNER + LEX_SPACE + LEX_JOIN),
        LEFT(LEX_LEFT + LEX_SPACE + LEX_JOIN),
        LEFT_OUTER(LEX_LEFT + LEX_SPACE + LEX_OUTER + LEX_SPACE + LEX_JOIN),
        RIGHT(LEX_RIGHT + LEX_SPACE + LEX_JOIN),
        RIGHT_OUTER(LEX_RIGHT + LEX_SPACE + LEX_OUTER + LEX_SPACE + LEX_JOIN),
        FULL(LEX_FULL + LEX_SPACE + LEX_JOIN),
        FULL_OUTER(LEX_FULL + LEX_SPACE + LEX_OUTER + LEX_SPACE + LEX_JOIN),
        CROSS(LEX_CROSS + LEX_SPACE + LEX_JOIN),
        NATURAL(LEX_NATURAL + LEX_SPACE + LEX_JOIN),
        NATURAL_INNER(LEX_NATURAL + LEX_SPACE + LEX_INNER + LEX_SPACE + LEX_JOIN),
        NATURAL_LEFT(LEX_NATURAL + LEX_SPACE + LEX_LEFT + LEX_SPACE + LEX_JOIN),
        NATURAL_LEFT_OUTER(LEX_NATURAL + LEX_SPACE + LEX_LEFT + LEX_SPACE + LEX_OUTER + LEX_SPACE + LEX_JOIN),
        NATURAL_RIGHT(LEX_NATURAL + LEX_SPACE + LEX_RIGHT + LEX_SPACE + LEX_JOIN),
        NATURAL_RIGHT_OUTER(LEX_NATURAL + LEX_SPACE + LEX_RIGHT + LEX_SPACE + LEX_OUTER + LEX_SPACE + LEX_JOIN),
        NATURAL_FULL(LEX_NATURAL + LEX_SPACE + LEX_FULL + LEX_SPACE + LEX_JOIN),
        NATURAL_FULL_OUTER(LEX_NATURAL + LEX_SPACE + LEX_FULL + LEX_SPACE + LEX_OUTER + LEX_SPACE + LEX_JOIN);

        private final String fullLexeme;

        JoinType(String fullLexeme) {
            this.fullLexeme = fullLexeme;
        }

        public static final Map<String, JoinType> joinTypeFullLexemes = MultiLex.createLexemeMap(JoinType.class);
    }

    @Override
    public String toString() {
       return QueryPrinter.printJoin(this);
    }
}
