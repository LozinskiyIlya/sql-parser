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
    private List<Column> groupings = new LinkedList<>();
    private List<Sort> sorts = new LinkedList<>();
    private Integer limit = LIMIT_ALL;
    private Integer offset = NO_OFFSET;
    private String alias;

    @Override
    public String getValue() {
        return toString();
    }

    @Override
    public String toString() {
        return QueryPrinter.print(this).toLowerCase();
    }

    static class QueryPrinter {
        public static String print(Query query) {
            final var builder = new LinkedList<String>();
            builder.add(LEX_SELECT);
            builder.add(printFragmentsList(query.columns));
            builder.add(printSources(query.sources));
            query.getFilters().stream().map(Condition::toString).forEach(builder::add);
            // joins
            // groupings
            // sorts
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

    private static String printSources(List<Source> sources) {
        if (sources.isEmpty()) {
            return "";
        }
        final var casted = sources.stream().map(Fragment.class::cast).toList();
        return LEX_FROM + LEX_SPACE + printFragmentsList(casted);
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
