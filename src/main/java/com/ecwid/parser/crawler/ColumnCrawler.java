package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.AliasCleaner;
import com.ecwid.parser.fragment.enity.Column;
import com.ecwid.parser.fragment.enity.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Component
public class ColumnCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String select, Supplier<String> nextFragmentSupplier) {
        String nextFragment;
        final var aliasCleaner = new AliasCleaner();
        while ((nextFragment = nextFragmentSupplier.get()) != null) {
            if (shouldDelegate(nextFragment)) {
                flush(query, aliasCleaner);
                delegate(query, nextFragment, nextFragmentSupplier);
                return;
            }
            if (LEX_COMMA.equals(nextFragment)) {
                flush(query, aliasCleaner);
                continue;
            }
            if (LEX_OPEN_BRACKET.equals(nextFragment)) {
                nextFragment = crawlFunction(aliasCleaner, nextFragmentSupplier);
            }
            aliasCleaner.push(nextFragment);
        }
    }

    private String crawlFunction(AliasCleaner aliasCleaner, Supplier<String> nextFragmentSupplier) {
        final var functionBuilder = new StringBuilder();
        if (StringUtils.hasText(aliasCleaner.name())) {
            // the last inserted value was a function name
            functionBuilder.append(aliasCleaner.name());
            aliasCleaner.reset();
        }
        functionBuilder.append(LEX_OPEN_BRACKET);
        crawlUntilAndReturnNext(LEX_CLOSE_BRACKET::equals, functionBuilder::append, nextFragmentSupplier);
        functionBuilder.append(LEX_CLOSE_BRACKET);
        return functionBuilder.toString();
    }

    private void flush(Query query, AliasCleaner aliasCleaner) {
        query.getColumns().add(new Column(aliasCleaner.name(), aliasCleaner.alias()));
        aliasCleaner.reset();
    }
}