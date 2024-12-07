package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.IntegerCrawler;
import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

import static com.ecwid.parser.Lexemes.LEX_LIMIT;

@Component
public class LimitCrawler extends IntegerCrawler {

    public LimitCrawler() {
        super(Query::setLimit, LEX_LIMIT);
    }
}
