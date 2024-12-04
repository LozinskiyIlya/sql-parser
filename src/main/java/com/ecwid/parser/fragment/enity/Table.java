package com.ecwid.parser.fragment.enity;

import com.ecwid.parser.fragment.source.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public record Table(String name, String alias) implements Source, Nameable {
}
