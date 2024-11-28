package com.ecwid.parser;

import lombok.Data;

@Data
public class Sort {

    private Direction direction = Direction.ASC;
    private Nulls nulls = Nulls.LAST;
    private String column;

    enum Direction {
        ASC,
        DESC
    }

    enum Nulls {
        FIRST,
        LAST
    }

}
