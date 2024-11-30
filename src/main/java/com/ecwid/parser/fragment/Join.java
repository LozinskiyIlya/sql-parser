package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.enity.Column;
import com.ecwid.parser.fragment.enity.Table;
import lombok.Data;

@Data
public class Join {
    private JoinType type;
    private Table table;
    private Column leftColumn;
    private Column rightColumn;

    enum JoinType {
        INNER,
        LEFT,
        RIGHT,
        FULL
    }
}
