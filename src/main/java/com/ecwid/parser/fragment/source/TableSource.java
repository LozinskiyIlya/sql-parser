package com.ecwid.parser.fragment.source;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableSource implements Source {
    private String tableName;
}
