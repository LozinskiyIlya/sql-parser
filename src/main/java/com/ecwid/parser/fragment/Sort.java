package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Fragment;
import lombok.Data;

import java.util.LinkedList;

import static com.ecwid.parser.Lexemes.*;

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
        final var builder = new LinkedList<String>();
        builder.add(sortBy.toString());
        builder.add(direction.name());
        builder.add(LEX_NULLS.toUpperCase());
        builder.add(nulls.name());
        return String.join(LEX_SPACE, builder);
    }
}
