package com.ecwid.parser.fragments;

import lombok.Data;

@Data
public class Join implements Fragment{
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
