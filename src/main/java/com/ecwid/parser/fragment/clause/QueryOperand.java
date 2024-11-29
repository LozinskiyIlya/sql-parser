package com.ecwid.parser.fragment.clause;


import lombok.Data;

import javax.management.Query;

@Data
public class QueryOperand implements Operand {
    private Query query;
}
