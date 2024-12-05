package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Column;
import lombok.Data;

@Data
public class Sort {

    private Direction direction = Direction.ASC;
    private Nulls nulls = Nulls.LAST;
    private Column column;

    enum Direction {
        ASC,
        DESC
    }

    enum Nulls {
        FIRST,
        LAST
    }

}
