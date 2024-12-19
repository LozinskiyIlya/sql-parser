package com.ecwid.parser.crawler;

import com.ecwid.parser.config.LexemeHandler;
import com.ecwid.parser.crawler.base.SkipClauseCrawler;
import com.ecwid.parser.fragment.Sort;

import static com.ecwid.parser.Lexemes.LEX_ORDER;

@LexemeHandler(lexemes = LEX_ORDER)
public class OrderCrawler extends SkipClauseCrawler {

    public OrderCrawler() {
        super((query, fragment) -> {
            final var sort = new Sort();
            sort.setSortBy(fragment);
            query.getSorts().add(sort);
        });
    }
}
