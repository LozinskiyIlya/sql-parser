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
public class Query implements Operand, Source {
    private List<Column> columns = new LinkedList<>();
    private List<Source> sources = new LinkedList<>();
    private List<Join> joins = new LinkedList<>();
    private List<WhereClause> filters = new LinkedList<>();
    private List<Column> groupings = new LinkedList<>();
    private List<Sort> sorts = new LinkedList<>();
    private Integer limit;
    private Integer offset;
    private String alias;

    @Override
    public String alias() {
        return alias;
    }
}
