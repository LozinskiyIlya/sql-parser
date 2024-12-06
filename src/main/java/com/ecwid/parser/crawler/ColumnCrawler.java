package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.NameAliasPair;
import com.ecwid.parser.fragment.domain.Column;
import com.ecwid.parser.fragment.domain.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Component
public class ColumnCrawler extends CanHaveFunctionCrawler {

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
                nextFragment = getFunctionSignature(pair, fragments);
            }
            pair.push(nextFragment);
        }
    }

    private void flush(Query query, NameAliasPair pair) {
        query.getColumns().add(new Column(pair.getFirst(), pair.getSecond()));
        pair.reset();
    }
}