package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.clause.WhereClause;
import lombok.Data;
import com.ecwid.parser.fragment.source.Source;

import java.util.LinkedList;
import java.util.List;


@Data
public class Query {
    private List<String> columns = new LinkedList<>();
    private List<Source> fromSources = new LinkedList<>();
    private List<Join> joins = new LinkedList<>();
    private List<WhereClause> whereClauses = new LinkedList<>();
    private List<String> groupByColumns = new LinkedList<>();
    private List<Sort> sortColumns = new LinkedList<>();
    private Integer limit;
    private Integer offset;
}
