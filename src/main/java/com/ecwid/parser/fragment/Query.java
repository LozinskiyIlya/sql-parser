package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Source;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ecwid.parser.Lexemes.*;

@Data
public class Query implements Source {
    private List<Column> columns = new LinkedList<>();
    private List<Source> sources = new LinkedList<>();
    private List<Join> joins = new LinkedList<>();
    private List<Condition> filters = new LinkedList<>();
    private List<Column> groupings = new LinkedList<>();
    private List<Sort> sorts = new LinkedList<>();
    private Integer limit;
    private Integer offset;
    private String alias;
    @Override
    public String toString() {
        return QueryPrinter.print(this).toLowerCase();
    }

    static class QueryPrinter {
        public static String print(Query query) {
            final var builder = new LinkedList<String>();
            builder.add(LEX_SELECT);
            builder.add(printColumns(query));
            builder.add(LEX_FROM);
            builder.add(printSources(query));
            query.getFilters().stream().map(Condition::toString).forEach(builder::add);
            // joins
            // groupings
            // sorts
            if (query.limit != null) {
                builder.add(LEX_LIMIT);
                builder.add(query.limit.toString());
            }
            if (query.offset != null) {
                builder.add(LEX_OFFSET);
                builder.add(query.offset.toString());
            }
            if (StringUtils.hasText(query.getAlias())) {
                builder.add(query.getAlias());
            }
            return String.join(LEX_SPACE, builder);
        }
    }

    private static String printColumns(Query query) {
        return query.getColumns().stream().map(Column::toString).collect(Collectors.joining(", "));
    }

    private static String printSources(Query query) {
        return query.getSources().stream().map(source -> {
            if (source instanceof Query) {
                String sourceAsString = source.toString();
                String alias = source.getAlias();

                if (StringUtils.hasText(alias)) {
                    // Ensure the alias is not already part of the sourceAsString
                    if (sourceAsString.endsWith(LEX_SPACE + alias)) {
                        sourceAsString = sourceAsString.substring(0, sourceAsString.lastIndexOf(LEX_SPACE + alias));
                    }
                    return LEX_OPEN_BRACKET + sourceAsString + LEX_CLOSE_BRACKET + LEX_SPACE + alias;
                }
                return LEX_OPEN_BRACKET + sourceAsString + LEX_CLOSE_BRACKET;
            }

            return source.toString();
        }).collect(Collectors.joining(", "));
    }
}
