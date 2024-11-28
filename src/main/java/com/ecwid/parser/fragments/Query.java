package com.ecwid.parser.fragments;

import com.ecwid.parser.fragments.clause.WhereClause;
import lombok.Data;
import com.ecwid.parser.fragments.source.Source;

import java.util.List;


@Data
public class Query  {
    private List<String> columns;
    private List<Source> fromSources;
    private List<Join> joins;
    private List<WhereClause> whereClauses;
    private List<String> groupByColumns;
    private List<Sort> sortColumns;
    private Integer limit;
    private Integer offset;
}
