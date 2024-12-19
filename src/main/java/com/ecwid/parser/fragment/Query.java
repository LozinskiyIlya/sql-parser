package com.ecwid.parser.fragment;

import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Source;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ecwid.parser.Lexemes.*;

@Data
public class Query implements Source {
    private static final int LIMIT_ALL = -1;
    private static final int NO_OFFSET = 0;

    private List<Fragment> columns = new LinkedList<>();
    private List<Source> sources = new LinkedList<>();
    private List<Join> joins = new LinkedList<>();
    private List<Condition> filters = new LinkedList<>();
    private List<Fragment> groupings = new LinkedList<>();
    private List<Sort> sorts = new LinkedList<>();
    private Integer limit = LIMIT_ALL;
    private Integer offset = NO_OFFSET;
    private String alias;

    private QueryType getQueryType() {
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
        return QueryPrinter.print(this).toLowerCase();
    }

    public enum QueryType {
        SELECT,
        NESTED_JOIN,
        INSERT,
        UPDATE,
        DELETE
    }

    static class QueryPrinter {
        public static String print(Query query) {
            final var builder = new LinkedList<String>();
            printColumns(query, builder);
            printSources(query, builder);
            query.getFilters().stream().map(Condition::toString).forEach(builder::add);
            query.getJoins().stream().map(Join::toString).forEach(builder::add);
            //todo groupings
            //todo sorts
            if (query.limit != LIMIT_ALL) {
                builder.add(LEX_LIMIT);
                builder.add(query.limit.toString());
            }
            if (query.offset != NO_OFFSET) {
                builder.add(LEX_OFFSET);
                builder.add(query.offset.toString());
            }
            if (StringUtils.hasText(query.getAlias())) {
                builder.add(query.getAlias());
            }
            return String.join(LEX_SPACE, builder);
        }
    }

    private static void printColumns(Query query, List<String> builder) {
        if (query.getQueryType().equals(QueryType.NESTED_JOIN)) {
            // nested joins do not have select <columns> clause
            return;
        }
        builder.add(LEX_SELECT);
        builder.add(printFragmentsList(query.getColumns()));
    }

    private static void printSources(Query query, List<String> builder) {
        final var sources = query.getSources();
        if (sources.isEmpty()) {
            return;
        }
        final var casted = sources.stream().map(Fragment.class::cast).toList();
        if (!query.getQueryType().equals(QueryType.NESTED_JOIN)) {
            // nested joins do not have from <sources> clause
            builder.add(LEX_FROM);
        }
        builder.add(printFragmentsList(casted));
    }

    private static String printFragmentsList(List<Fragment> fragments) {
        return fragments.stream().map(fragment -> {
            if (fragment instanceof Query) {
                String sourceAsString = fragment.toString();
                String alias = ((Query) fragment).getAlias();

                if (StringUtils.hasText(alias)) {
                    if (sourceAsString.endsWith(LEX_SPACE + alias)) {
                        sourceAsString = sourceAsString.substring(0, sourceAsString.lastIndexOf(LEX_SPACE + alias));
                    }
                    return LEX_OPEN_BRACKET + sourceAsString + LEX_CLOSE_BRACKET + LEX_SPACE + alias;
                }
                return LEX_OPEN_BRACKET + sourceAsString + LEX_CLOSE_BRACKET;
            }

            return fragment.toString();
        }).collect(Collectors.joining(", ")).trim();
    }
}
