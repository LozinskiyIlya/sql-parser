package com.ecwid.parser.fragment;

import lombok.Data;

@Data
public class Join {
    private JoinType type;
    private String table;
    private String leftColumn;
    private String rightColumn;

    enum JoinType {
        INNER,
        LEFT,
        RIGHT,
        FULL
    }
}
