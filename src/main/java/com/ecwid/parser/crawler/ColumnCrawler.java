package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;



@Component
public class ColumnCrawler extends SectionAwareCrawler {

    @Override
    void addQueryFragment(Query query, String currentSection, Supplier<String> fragmentSupplier) {
        String nextFragment;
        while ((nextFragment = fragmentSupplier.get()) != null) {
            if (QUERY_SECTIONS.containsKey(nextFragment)) {
                delegateToNext(query, nextFragment, fragmentSupplier);
                return;
            }
            query.getColumns().add(nextFragment);
        }
    }
}
