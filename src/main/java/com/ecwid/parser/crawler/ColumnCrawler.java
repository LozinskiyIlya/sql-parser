package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_SELECT;
import static com.ecwid.parser.Lexemes.QUERY_SECTIONS;


@Component
public class ColumnCrawler extends SectionAwareCrawler {

    @Autowired
    public ColumnCrawler(final SourceCrawler next) {
        super(next, LEX_SELECT);
    }

    @Override
    void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (QUERY_SECTIONS.contains(nextFragment)) {
                delegateToNext(query, nextFragment, fragmentSupplier);
                return;
            }
            query.getColumns().add(nextFragment);
        }
    }

    @Override
    boolean isOptional() {
        return false;
    }
}
