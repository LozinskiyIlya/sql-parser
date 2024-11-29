package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.source.QuerySource;
import com.ecwid.parser.fragment.source.Source;
import com.ecwid.parser.fragment.source.TableSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_SELECT;

@Component
@RequiredArgsConstructor
class SourceCrawler extends SectionAwareCrawler {

    private final ColumnCrawler columnCrawler;

    @Override
    public void crawl(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            Source source;
            if (LEX_SELECT.equals(nextFragment)) {
                final var nestedQuery = new Query();
                source = new QuerySource(nestedQuery);
                columnCrawler.crawl(nestedQuery, nextFragment, fragmentSupplier);
            } else if (QUERY_SECTIONS.containsKey(nextFragment)) {
                delegateToNext(query, nextFragment, fragmentSupplier);
                return;
            } else {
                source = new TableSource(nextFragment);
            }

            query.getFromSources().add(source);
        }
    }
}
