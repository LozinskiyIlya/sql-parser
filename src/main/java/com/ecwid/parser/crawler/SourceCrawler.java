package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.source.QuerySource;
import com.ecwid.parser.fragment.source.Source;
import com.ecwid.parser.fragment.source.TableSource;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

public class SourceCrawler implements Crawler {

    private Crawler next;
    private final ColumnCrawler columnCrawler = new ColumnCrawler(this);

    @Override
    public void addQueryFragment(Query query, String currentCommand, Supplier<String> fragmentSupplier) {
        if (!myCommand().equals(currentCommand)) {
            throw new IllegalStateException("Unexpected token ^" + currentCommand);
        }
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (COMMANDS.contains(nextFragment)) {
                if (next != null) {
                    next.addQueryFragment(query, nextFragment, fragmentSupplier);
                }
                return;
            }
            Source source;
            if (LEX_SELECT.equals(nextFragment)) {
                final var nestedQuery = new Query();
                source = new QuerySource(nestedQuery);
                columnCrawler.addQueryFragment(nestedQuery, nextFragment, fragmentSupplier);
            } else {
                source = new TableSource(nextFragment);
            }
            query.getFromSources().add(source);
        }
    }

    @Override
    public Crawler next() {
        return next;
    }

    @Override
    public String myCommand() {
        return LEX_FROM;
    }
}
