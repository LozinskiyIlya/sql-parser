package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Column;
import com.ecwid.parser.fragment.enity.Query;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Component
public class ColumnCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String select, Supplier<String> nextFragmentSupplier) {
        String nextFragment;
        Pair columnAndAlias = new Pair();
        while ((nextFragment = nextFragmentSupplier.get()) != null) {
            if (shouldDelegate(nextFragment)) {
                flush(query, columnAndAlias);
                delegate(query, nextFragment, nextFragmentSupplier);
                return;
            }
            if (LEX_COMMA.equals(nextFragment)) {
                flush(query, columnAndAlias);
                continue;
            }
            if (LEX_OPEN_BRACKET.equals(nextFragment)) {
                nextFragment = crawlFunction(columnAndAlias, nextFragment, nextFragmentSupplier);
            }
            columnAndAlias.set(nextFragment);
        }
    }

    private String crawlFunction(Pair columnAndAlias, String currentFragment, Supplier<String> nextFragmentSupplier) {
        final var functionBuilder = new StringBuilder(currentFragment);
        if (StringUtils.hasText(columnAndAlias.getName())) {
            // the last inserted value was a function name
            functionBuilder.insert(0, columnAndAlias.getName());
            columnAndAlias.reset();
        }
        crawlUntilAndReturnNext(LEX_CLOSE_BRACKET::equals, functionBuilder::append, nextFragmentSupplier);
        functionBuilder.append(LEX_CLOSE_BRACKET);
        return functionBuilder.toString();
    }

    private void flush(Query query, Pair columnAndAlias) {
        query.getColumns().add(new Column(columnAndAlias.getName(), columnAndAlias.getAlias()));
        columnAndAlias.reset();
    }

    @Getter
    private static class Pair {
        private String name;
        private String alias;

        void set(String value) {
            if (name == null) {
                name = value;
            } else {
                alias = value;
            }
        }

        void reset() {
            name = null;
            alias = null;
        }
    }
}

// select a from
// select a b from
// select a b, c from
// select a, b from