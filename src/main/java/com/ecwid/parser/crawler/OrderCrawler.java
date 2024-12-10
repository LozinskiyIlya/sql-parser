package com.ecwid.parser.crawler;

import com.ecwid.parser.config.TriggerMeOn;
import com.ecwid.parser.crawler.base.NoClauseProcessCrawler;
import com.ecwid.parser.fragment.Sort;

import static com.ecwid.parser.Lexemes.LEX_ORDER;

@TriggerMeOn(lexemes = LEX_ORDER)
public class OrderCrawler extends NoClauseProcessCrawler {

    public OrderCrawler() {
        super((query, fragment) -> {
            final var sort = new Sort();
            sort.setFragment(fragment);
            query.getSorts().add(sort);
        });
    }
}
