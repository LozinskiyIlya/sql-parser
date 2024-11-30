package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ColumnCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String select, Supplier<String> fragmentSupplier) {
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (shouldDelegate(nextFragment)) {
                delegate(query, nextFragment, fragmentSupplier);
                return;
            }
            query.getColumns().add(nextFragment);
        }
    }
}
