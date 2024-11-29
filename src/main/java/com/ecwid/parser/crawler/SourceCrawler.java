package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.source.QuerySource;
import com.ecwid.parser.fragment.source.Source;
import com.ecwid.parser.fragment.source.TableSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Component
class SourceCrawler extends SectionAwareCrawler {

    private final ColumnCrawler columnCrawler;

    @Autowired
    public SourceCrawler(final LimitCrawler next, @Lazy final ColumnCrawler columnCrawler) {
        super(next, LEX_FROM);
        this.columnCrawler = columnCrawler;
    }

    @Override
    void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (QUERY_SECTIONS.contains(nextFragment)) {
                delegateToNext(query, nextFragment, fragmentSupplier);
                return;
            }
            Source source;
            if (LEX_SELECT.equals(nextFragment)) {
                final var nestedQuery = new Query();
                source = new QuerySource(nestedQuery);
                columnCrawler.addFragment(nestedQuery, nextFragment, fragmentSupplier);
            } else {
                source = new TableSource(nextFragment);
            }
            query.getFromSources().add(source);
        }
    }

    @Override
    boolean isOptional() {
        return false;
    }
}
