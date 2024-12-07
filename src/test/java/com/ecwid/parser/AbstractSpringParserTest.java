package com.ecwid.parser;


import com.ecwid.parser.config.ParserApplicationConfig;
import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.ConstantListOperand;
import com.ecwid.parser.fragment.ConstantOperand;
import com.ecwid.parser.fragment.Column;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Aliasable;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Nameable;
import com.ecwid.parser.service.SqlParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ecwid.parser.Lexemes.LEX_SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = ParserApplicationConfig.class)
@DisplayName("Should parse SQL")
public abstract class AbstractSpringParserTest {
    protected final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected SqlParser sqlParser;

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
        assertEquals(value, actual.getValue());
        if (value instanceof List) {
            assertEquals(type, actual.getClass());
            return;
        }
        assertAliasableEquals((Aliasable) actual, (Class<? extends Aliasable>) type, alias);
    }

    protected void assertNameableEquals(Nameable nameable, Class<? extends Nameable> type, String name, String alias) {
        assertAliasableEquals(nameable, type, alias);
        assertEquals(name, nameable.getName());
    }

    protected void assertAliasableEquals(Aliasable aliasable, Class<? extends Aliasable> type, String alias) {
        assertEquals(type, aliasable.getClass());
        assertEquals(alias, aliasable.getAlias());
    }

}
