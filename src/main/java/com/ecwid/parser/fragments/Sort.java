package com.ecwid.parser.fragments;

import lombok.Data;

@Data
public class Sort implements Fragment{

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
