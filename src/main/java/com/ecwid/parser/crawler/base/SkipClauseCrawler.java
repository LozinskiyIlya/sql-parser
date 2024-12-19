package com.ecwid.parser.crawler.base;

import com.ecwid.parser.crawler.base.helper.CrawlContext;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;

@RequiredArgsConstructor
public abstract class SkipClauseCrawler extends FragmentCrawler {

    protected final BiConsumer<Query, Fragment> onFragment;

    @Override
    protected final void onFragment(Query query, Fragment fragment) {
        onFragment.accept(query, fragment);
    }

    @Override
    protected final void onClause(CrawlContext context) {
        context.move();
    }
}
