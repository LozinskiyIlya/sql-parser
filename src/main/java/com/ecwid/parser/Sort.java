package com.ecwid.parser;

import lombok.Data;

@Data
public class Sort {

    private Direction direction;
    private String column;

    enum Direction {
        ASC,
        DESC
    }

}
