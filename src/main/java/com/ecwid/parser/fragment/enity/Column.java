package com.ecwid.parser.fragment.enity;

import com.ecwid.parser.fragment.clause.Operand;
import lombok.*;

public record Column(String name, String alias) implements Operand, Aliasable {
}
