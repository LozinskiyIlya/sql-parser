package com.ecwid.parser.fragment;

import com.ecwid.parser.QueryPrinter;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Source;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

import static com.ecwid.parser.QueryConstants.LIMIT_ALL;
import static com.ecwid.parser.QueryConstants.NO_OFFSET;

@Data
public class Query implements Source {
    private List<Fragment> columns = new LinkedList<>();
    private List<Source> sources = new LinkedList<>();
    private List<Join> joins = new LinkedList<>();
    private List<Condition> filters = new LinkedList<>();
    private List<Fragment> groupings = new LinkedList<>();
    private List<Sort> sorts = new LinkedList<>();
    private Integer limit = LIMIT_ALL;
    private Integer offset = NO_OFFSET;
    private String alias;

    @JsonIgnore
    public QueryType getQueryType() {
        if (columns.isEmpty()) {
            return QueryType.NESTED_JOIN;
        }
        return QueryType.SELECT;
    }

    @Override
    public String getValue() {
        return toString();
    }

    @Override
    public String toString() {
        return QueryPrinter.print(this);
    }

    public enum QueryType {
        SELECT,
        NESTED_JOIN,
        INSERT,
        UPDATE,
        DELETE
    }
}
