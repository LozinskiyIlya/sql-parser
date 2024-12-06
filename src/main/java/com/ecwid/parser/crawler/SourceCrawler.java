package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.NameAliasPair;
import com.ecwid.parser.fragment.domain.Query;
import com.ecwid.parser.fragment.domain.Table;
import com.ecwid.parser.fragment.source.Source;
import org.springframework.stereotype.Component;

import java.util.Stack;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Component
public class SourceCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String from, Supplier<String> fragments) {
        String nextFragment;
        final var sources = new Stack<Source>();
        final var pair = new NameAliasPair();
        while ((nextFragment = fragments.get()) != null) {
            if (LEX_CLOSE_BRACKET.equals(nextFragment)) {
                break;
            }
            if (LEX_OPEN_BRACKET.equals(nextFragment)) {
                continue;
            }
            if (LEX_COMMA.equals(nextFragment)) {
                flush(query, sources, pair);
                continue;
            }
            if (LEX_SELECT.equals(nextFragment)) {
                final var nested = new Query();
                nextCrawler(nextFragment).orElseThrow().crawl(nested, nextFragment, fragments);
                sources.push(nested);
                continue;
            }
            if (shouldDelegate(nextFragment)) {
                delegate(query, nextFragment, fragments);
                flush(query, sources, pair);
                return;
            }
            if (sources.isEmpty()) {
                sources.push(new Table());
            }
            pair.push(nextFragment);
        }

        // flushing the last source when:
        // 1) at a nested query ')' is reached
        // 2) query is not closed with a semicolon
        flush(query, sources, pair);
    }

    private void flush(Query query, Stack<Source> sources, NameAliasPair nameAliasPair) {
        final var source = sources.pop();
        if (source instanceof Table) {
            ((Table) source).setName(nameAliasPair.getFirst());
            ((Table) source).setAlias(nameAliasPair.getSecond());
        } else {
            ((Query) source).setAlias(nameAliasPair.getFirst());
        }
        query.getSources().add(source);
        nameAliasPair.reset();
        if (!sources.isEmpty()) {
            throw new IllegalStateException("Syntax error at FROM clause. Expected comma or semicolon.");
        }
    }
}
