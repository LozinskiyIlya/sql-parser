package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.IntegerCrawler;
import com.ecwid.parser.fragment.Query;
import org.springframework.stereotype.Component;

@Component
public class LimitCrawler extends IntegerCrawler {

    public LimitCrawler() {
        super(Query::setLimit, "LIMIT");
    }
}
