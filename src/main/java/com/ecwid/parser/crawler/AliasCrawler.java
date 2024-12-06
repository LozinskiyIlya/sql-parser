package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.domain.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.LEX_AS;

@Component
public class AliasCrawler implements Crawler {
    @Override
    public Optional<Crawler> nextCrawler(String nextSection) {
        return Optional.empty();
    }

    @Override
    public void crawl(Query query, String currentSection, Supplier<String> fragments) {
        throw new UnsupportedOperationException("Call crawlForAlias instead of this method");
    }

    public String crawForAlias(Supplier<String> fragments, Consumer<String> setAlias) {
        String nextFragment = fragments.get();
        if (!LEX_AS.equals(nextFragment)) {
            return nextFragment;
        }
        nextFragment = fragments.get();
        setAlias.accept(nextFragment);
        return fragments.get();
    }
}
