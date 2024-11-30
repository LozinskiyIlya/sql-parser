package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.source.QuerySource;
import com.ecwid.parser.fragment.source.Source;
import com.ecwid.parser.fragment.source.TableSource;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_OPEN_BRACKET;
import static com.ecwid.parser.Lexemes.LEX_SELECT;

@Component
class SourceCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String from, Supplier<String> fragmentSupplier) {
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (nextFragment.equals(LEX_OPEN_BRACKET)) {
                continue;
            }
            Source source;
            if (LEX_SELECT.equals(nextFragment)) {
                final var nestedQuery = new Query();
                source = new QuerySource(nestedQuery);
                selectCrawler(nextFragment).crawl(nestedQuery, nextFragment, fragmentSupplier);
            } else if (shouldDelegate(nextFragment)) {
                delegate(query, nextFragment, fragmentSupplier);
                return;
            } else {
                source = new TableSource(nextFragment);
            }
            query.getFromSources().add(source);
        }
    }
}
