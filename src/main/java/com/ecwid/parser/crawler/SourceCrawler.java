package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.source.QuerySource;
import com.ecwid.parser.fragment.source.Source;
import com.ecwid.parser.fragment.source.TableSource;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

public class SourceCrawler extends SectionAwareCrawler {

    private final ColumnCrawler columnCrawler = new ColumnCrawler(this);

    public SourceCrawler(Crawler next) {
        super(next, LEX_FROM);
    }

    @Override
    public void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (QUERY_SECTIONS.contains(nextFragment)) {
                delegateToNextCrawler(query, nextFragment, fragmentSupplier);
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
}
