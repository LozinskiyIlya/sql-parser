package com.ecwid.parser;


import com.ecwid.parser.config.ParserApplicationConfig;
import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Aliasable;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.service.SqlParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

import static com.ecwid.parser.Lexemes.LEX_SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = ParserApplicationConfig.class)
@DisplayName("Should parse SQL")
public abstract class AbstractSpringParserTest {

    @Autowired
    protected SqlParser sqlParser;

    @Test
    void withAllThatBeauty() throws IOException {
        throw new UnsupportedOperationException("Implement all possible cases test for: " + this.getClass().getSimpleName());
    }

    protected void assertConditionEquals(
            Condition.ClauseType type,
            Class<? extends Fragment> leftType,
            Object leftVal,
            Condition.Operator operator,
            Class<? extends Fragment> rightType,
            Object rightVal,
            Condition actual) {
        assertEquals(type, actual.getClauseType(), "Clause type mismatch");
        assertEquals(operator, actual.getOperator(), "Operator mismatch");
        final var leftOperand = actual.getLeftOperand();
        final var rightOperand = actual.getRightOperand();
        assertFragmentEquals(leftType, leftVal, null, leftOperand);
        assertFragmentEquals(rightType, rightVal, null, rightOperand);
    }

    @SuppressWarnings("unchecked")
    protected void assertFragmentEquals(
            Class<? extends Fragment> type,
            Object value,
            String alias,
            Fragment actual
    ) {
        if (value instanceof String) {
            value = ((String) value).toLowerCase();
        }
        if (actual instanceof Query && StringUtils.hasText(((Query) actual).getAlias())) {
            value += LEX_SPACE + ((Query) actual).getAlias();
        }
        assertEquals(value, actual.getValue(), actual.getClass().getSimpleName() + " value mismatch");
        if (value instanceof List) {
            assertEquals(type, actual.getClass(), "Fragment type mismatch");
            return;
        }
        assertAliasableEquals((Aliasable) actual, (Class<? extends Aliasable>) type, alias);
    }

    protected void assertAliasableEquals(Aliasable aliasable, Class<? extends Aliasable> type, String alias) {
        assertEquals(type, aliasable.getClass(), "Aliasable type mismatch");
        assertEquals(alias, aliasable.getAlias(), "Alias mismatch");
    }

    protected void assertEqualsIgnoreCaseTrimmed(String expected, String actual) {
        assertEquals(expected.toLowerCase().trim(), actual.toLowerCase().trim());
    }

    public record TestCase(String displayName, String input, List<String> expected) {
    }
}
