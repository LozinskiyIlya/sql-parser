package com.ecwid.parser.fragment.clause;

import com.ecwid.parser.fragment.Query;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueryOperand implements Operand {
    private Query query;
}
