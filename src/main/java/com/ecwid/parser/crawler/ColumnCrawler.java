package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Column;
import com.ecwid.parser.fragment.enity.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
public class ColumnCrawler extends SectionAwareCrawler implements FunctionAwareListCrawler {

    {
        crawlUntil = fragment -> false;
        addToQuery = FunctionAwareListCrawler.super.addToQuery();
    }

    @Override
    public BiConsumer<Query, String> flushItem() {
        return (query, fragment) -> query.getColumns().add(new Column(fragment, null));
    }

    @Override
    public Function<Query, StringBuilder> acc() {
        return query ->
                Optional.of(query.getColumns().isEmpty())
                        .filter(Boolean.FALSE::equals)
                        .map(b -> query)
                        .map(Query::getColumns)
                        .map(List::removeLast)
                        .map(Column::name)
                        .map(StringBuilder::new)
                        .orElseGet(StringBuilder::new);

    }
}
