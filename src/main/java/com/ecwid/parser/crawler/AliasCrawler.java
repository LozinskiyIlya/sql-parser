package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.Query;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_AS;

@Component
public class AliasCrawler implements Crawler {
    @Override
    public Crawler nextCrawler(String nextSection) {
        return null;
    }

    @Override
    public void crawl(Query query, String currentSection, Supplier<String> nextFragmentSupplier) {
        throw new UnsupportedOperationException("Call crawlForAlias instead of this method");
    }

    public String crawForAlias(Supplier<String> nextFragmentSupplier, Consumer<String> setAlias) {
        String nextFragment = nextFragmentSupplier.get();
        if (!LEX_AS.equals(nextFragment)) {
            return nextFragment;
        }
        nextFragment = nextFragmentSupplier.get();
        setAlias.accept(nextFragment);
        return nextFragmentSupplier.get();
    }
}
