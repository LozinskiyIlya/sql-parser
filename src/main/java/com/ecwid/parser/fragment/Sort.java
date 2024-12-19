package com.ecwid.parser.fragment;

import com.ecwid.parser.QueryPrinter;
import com.ecwid.parser.fragment.domain.Fragment;
import lombok.Data;

@Data
public class Sort {

    private Direction direction = Direction.ASC;
    private Nulls nulls = Nulls.LAST;
    private Fragment sortBy;

    public enum Direction {
        ASC,
        DESC
    }

    public enum Nulls {
        FIRST,
        LAST
    }

    @Override
    public String toString() {
        return QueryPrinter.printSort(this);
    }
}
