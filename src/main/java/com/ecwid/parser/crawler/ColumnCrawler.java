package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import lombok.AllArgsConstructor;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.COMMANDS;
import static com.ecwid.parser.Lexemes.LEX_SELECT;

@AllArgsConstructor
public class ColumnCrawler implements Crawler {

    private SourceCrawler next;

    @Override
    public Crawler next() {
        return next;
    }

    @Override
    public String myCommand() {
        return LEX_SELECT;
    }

    @Override
    public void addQueryFragment(Query query, String command, Supplier<String> fragmentSupplier) {
        if (!myCommand().equals(command)) {
            throw new IllegalStateException("Unexpected token ^" + command);
        }
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (COMMANDS.contains(nextFragment)) {
                if (next != null) {
                    next.addQueryFragment(query, nextFragment, fragmentSupplier);
                }
                return;
            }
            query.getColumns().add(nextFragment);
        }
    }
}
