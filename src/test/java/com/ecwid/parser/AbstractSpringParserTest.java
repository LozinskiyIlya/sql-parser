package com.ecwid.parser;


import com.ecwid.parser.fragment.condition.Condition;
import com.ecwid.parser.fragment.condition.ConstantListOperand;
import com.ecwid.parser.fragment.condition.ConstantOperand;
import com.ecwid.parser.fragment.condition.Operand;
import com.ecwid.parser.fragment.domain.Column;
import com.ecwid.parser.fragment.domain.Query;
import com.ecwid.parser.service.SqlParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(SqlParserApplication.class)
@DisplayName("Should parse SQL")
public abstract class AbstractSpringParserTest {
    protected final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected SqlParser sqlParser;

    protected void assertConditionEquals(
            Condition.ClauseType type,
            Class<? extends Operand> leftType,
            Object leftVal,
            Condition.Operator operator,
            Class<? extends Operand> rightType,
            Object rightVal,
            Condition actual) {
        assertEquals(type, actual.getClauseType(), "Clause type mismatch");
        assertEquals(operator, actual.getOperator(), "Operator mismatch");
        final var leftOperand = actual.getLeftOperand();
        final var rightOperand = actual.getRightOperand();
        assertEquals(leftType, leftOperand.getClass(), "Left operand type mismatch");
        assertEquals(leftVal, getOperandValue(leftOperand), "Left operand value mismatch");
        assertEquals(rightType, rightOperand.getClass(), "Right operand type mismatch");
        assertEquals(rightVal, getOperandValue(rightOperand), "Right operand value mismatch");
    }


    protected Object getOperandValue(Operand operand) {
        return switch (operand) {
            case Column o -> o.name();
            case ConstantOperand o -> o.getValue();
            case Query o -> o.toString();
            case ConstantListOperand o -> o.getValues();
            default ->
                    throw new IllegalArgumentException("Operand type not supported: " + operand.getClass().getSimpleName());
        };
    }

}
