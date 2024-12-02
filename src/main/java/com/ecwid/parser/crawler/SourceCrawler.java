package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.enity.Query;
import com.ecwid.parser.fragment.enity.Table;
import com.ecwid.parser.fragment.source.Source;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_OPEN_BRACKET;
import static com.ecwid.parser.Lexemes.LEX_SELECT;

@Component
class SourceCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String from, Supplier<String> nextFragmentSupplier) {
        String nextFragment;
        while ((nextFragment = nextFragmentSupplier.get()) != null) {
            if (nextFragment.equals(LEX_OPEN_BRACKET)) {
                continue;
            }
            Source source;
            if (LEX_SELECT.equals(nextFragment)) {
                source = new Query();
                nextCrawler(nextFragment).crawl((Query) source, nextFragment, nextFragmentSupplier);
            } else if (shouldDelegate(nextFragment)) {
                delegate(query, nextFragment, nextFragmentSupplier);
                return;
            } else {
                source = new Table(nextFragment, null);
            }
            query.getSources().add(source);
        }
    }
}
