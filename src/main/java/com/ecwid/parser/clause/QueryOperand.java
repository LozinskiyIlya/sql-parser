package com.ecwid.parser.clause;


import lombok.Data;

import javax.management.Query;

@Data
public class QueryOperand implements Operand {
    private Query query;
}
