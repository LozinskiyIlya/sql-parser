package com.ecwid.parser.fragment.enity;

import com.ecwid.parser.fragment.Join;
import com.ecwid.parser.fragment.Sort;
import com.ecwid.parser.fragment.clause.Operand;
import com.ecwid.parser.fragment.clause.WhereClause;
import com.ecwid.parser.fragment.source.Source;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;


@Data
public class Query implements Operand, Source, Aliasable {
    private List<Column> columns = new LinkedList<>();
    private List<Source> fromSources = new LinkedList<>();
    private List<Join> joins = new LinkedList<>();
    private List<WhereClause> whereClauses = new LinkedList<>();
    private List<Column> groupByColumns = new LinkedList<>();
    private List<Sort> sortColumns = new LinkedList<>();
    private Integer limit;
    private Integer offset;
    private String alias;
}
