package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;

@RequiredArgsConstructor
public abstract class NoClauseProcessCrawler extends FragmentCrawler {

    protected final BiConsumer<Query, Fragment> onFragment;

    @Override
    protected final void processFragment(Query query, Fragment fragment) {
        onFragment.accept(query, fragment);
    }

    @Override
    protected final String processClauseAndReturnNextLex(CrawlContext context) {
        return context.lexSupplier().get();
    }
}
