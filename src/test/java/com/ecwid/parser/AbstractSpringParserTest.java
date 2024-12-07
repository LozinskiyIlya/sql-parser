package com.ecwid.parser;


import com.ecwid.parser.config.ParserApplicationConfig;
import com.ecwid.parser.fragment.Condition;
import com.ecwid.parser.fragment.ConstantListOperand;
import com.ecwid.parser.fragment.ConstantOperand;
import com.ecwid.parser.fragment.Column;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.service.SqlParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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
        if (leftVal instanceof String) {
            leftVal = ((String) leftVal).toLowerCase();
        }
        if (rightVal instanceof String) {
            rightVal = ((String) rightVal).toLowerCase();
        }
        assertEquals(leftType, leftOperand.getClass(), "Left operand type mismatch");
        assertEquals(leftVal, getOperandValue(leftOperand), "Left operand value mismatch");
        assertEquals(rightType, rightOperand.getClass(), "Right operand type mismatch");
        assertEquals(rightVal, getOperandValue(rightOperand), "Right operand value mismatch");
    }


    protected Object getOperandValue(Fragment operand) {
        return switch (operand) {
            case Column o -> o.getName();
            case ConstantOperand o -> o.getValue();
            case Query o -> o.toString();
            case ConstantListOperand o -> o.getValues();
            default ->
                    throw new IllegalArgumentException("Operand type not supported: " + operand.getClass().getSimpleName());
        };
    }

}
