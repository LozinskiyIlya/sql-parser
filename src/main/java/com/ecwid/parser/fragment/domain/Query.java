package com.ecwid.parser.fragment.domain;

import com.ecwid.parser.fragment.Join;
import com.ecwid.parser.fragment.Sort;
import com.ecwid.parser.fragment.clause.Operand;
import com.ecwid.parser.fragment.clause.Condition;
import com.ecwid.parser.fragment.source.Source;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

import static com.ecwid.parser.Lexemes.*;

@Data
public class Query implements Operand, Source {
    private List<Column> columns = new LinkedList<>();
    private List<Source> sources = new LinkedList<>();
    private List<Join> joins = new LinkedList<>();
    private List<Condition> filters = new LinkedList<>();
    private List<Column> groupings = new LinkedList<>();
    private List<Sort> sorts = new LinkedList<>();
    private Integer limit;
    private Integer offset;
    @Getter(AccessLevel.NONE)
    private String alias;

    @Override
    public String alias() {
        return alias;
    }

    @Override
    public String toString() {
        return QueryPrinter.print(this).toLowerCase();
    }

    static class QueryPrinter {
        public static String print(Query query) {
            final var builder = new LinkedList<String>();
            builder.add(LEX_SELECT);
            query.getColumns().stream().map(Column::toString).forEach(builder::add);
            builder.add(LEX_FROM);
            query.getSources().stream().map(Source::toString).forEach(builder::add);
            query.getFilters().stream().map(Condition::toString).forEach(builder::add);
            if (StringUtils.hasText(query.alias())) {
                builder.add(query.alias());
            }
            return String.join(LEX_SPACE, builder);
        }
    }
}
