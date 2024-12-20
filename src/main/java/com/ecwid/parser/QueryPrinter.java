package com.ecwid.parser;

import com.ecwid.parser.fragment.*;
import com.ecwid.parser.fragment.Condition.Operator;
import com.ecwid.parser.fragment.domain.Fragment;
import com.ecwid.parser.fragment.domain.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.QueryConstants.LIMIT_ALL;
import static com.ecwid.parser.QueryConstants.NO_OFFSET;

public class QueryPrinter {
    public static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @SneakyThrows
    public static String printJson(Query query) {
        return mapper.writeValueAsString(query);
    }

    public static String print(Query query) {
        final var builder = new LinkedList<String>();
        printColumns(query, builder);
        printSources(query, builder);
        query.getFilters().stream().map(Condition::toString).forEach(builder::add);
        query.getJoins().stream().map(Join::toString).forEach(builder::add);
        printGroupings(query, builder);
        printSorts(query, builder);
        if (query.getLimit() != LIMIT_ALL) {
            builder.add(LEX_LIMIT);
            builder.add(query.getLimit().toString());
        }
        if (query.getOffset() != NO_OFFSET) {
            builder.add(LEX_OFFSET);
            builder.add(query.getOffset().toString());
        }
        if (StringUtils.hasText(query.getAlias())) {
            builder.add(query.getAlias());
        }
        return String.join(LEX_SPACE, builder).toLowerCase();
    }

    public static String printJoin(Join join) {
        final var builder = new LinkedList<String>();
        builder.add(join.getJoinType().getFullLexeme());
        builder.add(bracketizeSourceIfNested(join.getSource()));
        join.getConditions().stream().map(Condition::toString).forEach(builder::add);
        return String.join(LEX_SPACE, builder);
    }

    public static String printCondition(Condition condition) {
        final var builder = new LinkedList<String>();
        builder.add(condition.getClauseType().name());
        printOperand(builder, condition.getLeftOperand());
        printOperator(builder, condition.getOperator());
        printOperand(builder, condition.getRightOperand());
        return String.join(LEX_SPACE, builder);
    }

    public static String printSort(Sort sort) {
        final var builder = new LinkedList<String>();
        builder.add(sort.getSortBy().toString());
        builder.add(sort.getDirection().name());
        builder.add(LEX_NULLS.toUpperCase());
        builder.add(sort.getNulls().name());
        return String.join(LEX_SPACE, builder);
    }

    private static void printColumns(Query query, List<String> builder) {
        if (query.getQueryType().equals(Query.QueryType.NESTED_JOIN)) {
            // nested joins do not have select <columns> clause
            return;
        }
        builder.add(LEX_SELECT);
        builder.add(printFragmentsList(query.getColumns()));
    }

    private static void printGroupings(Query query, List<String> builder) {
        if (query.getGroupings().isEmpty()) {
            return;
        }
        builder.add(LEX_GROUP);
        builder.add(LEX_BY);
        builder.add(printFragmentsList(query.getGroupings()));
    }

    private static void printSorts(Query query, List<String> builder) {
        if (query.getSorts().isEmpty()) {
            return;
        }
        builder.add(LEX_ORDER);
        builder.add(LEX_BY);
        query.getSorts().stream().map(QueryPrinter::printSort).forEach(builder::add);
    }

    private static void printSources(Query query, List<String> builder) {
        final var sources = query.getSources();
        if (sources.isEmpty()) {
            return;
        }
        final var casted = sources.stream().map(Fragment.class::cast).toList();
        if (!query.getQueryType().equals(Query.QueryType.NESTED_JOIN)) {
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

    private static String bracketizeSourceIfNested(Source source) {
        if (source instanceof Query) {
            return LEX_OPEN_BRACKET + clearFromAlias(source) + LEX_CLOSE_BRACKET + LEX_SPACE + source.getAlias();
        }
        return source.toString();
    }

    private static String clearFromAlias(Source source) {
        final var alias = source.getAlias();
        final var fullValue = source.toString();
        if (StringUtils.hasText(alias)) {
            return fullValue.substring(0, fullValue.lastIndexOf(LEX_SPACE));
        }
        return fullValue;
    }


    private static void printOperand(List<String> builder, Fragment operand) {
        if (operand == null) {
            return;
        }
        if (operand instanceof Query || operand instanceof ConstantList) {
            builder.add(LEX_OPEN_BRACKET);
            builder.add(operand.toString());
            builder.add(LEX_CLOSE_BRACKET);
        } else {
            builder.add(operand.toString());
        }
    }

    private static void printOperator(List<String> builder, Operator operator) {
        if (operator != null) {
            builder.add(operator.getFullLexeme());
        }
    }
}
