package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.NameAliasPair;
import com.ecwid.parser.fragment.domain.Column;
import com.ecwid.parser.fragment.domain.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Component
public class ColumnCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String select, Supplier<String> fragments) {
        String nextFragment;
        final var pair = new NameAliasPair();
        while ((nextFragment = fragments.get()) != null) {
            if (shouldDelegate(nextFragment)) {
                flush(query, pair);
                delegate(query, nextFragment, fragments);
                return;
            }
            if (LEX_COMMA.equals(nextFragment)) {
                flush(query, pair);
                continue;
            }
            if (LEX_OPEN_BRACKET.equals(nextFragment)) {
                nextFragment = crawlFunction(pair, fragments);
            }
            pair.push(nextFragment);
        }
    }

    private String crawlFunction(NameAliasPair pair, Supplier<String> fragments) {
        final var functionBuilder = new StringBuilder();
        if (StringUtils.hasText(pair.getFirst())) {
            // the last inserted value was a function name
            functionBuilder.append(pair.getFirst());
            pair.reset();
        }
        functionBuilder.append(LEX_OPEN_BRACKET);
        crawlUntilAndReturnNext(LEX_CLOSE_BRACKET::equals, functionBuilder::append, fragments);
        functionBuilder.append(LEX_CLOSE_BRACKET);
        return functionBuilder.toString();
    }

    private void flush(Query query, NameAliasPair pair) {
        query.getColumns().add(new Column(pair.getFirst(), pair.getSecond()));
        pair.reset();
    }
}