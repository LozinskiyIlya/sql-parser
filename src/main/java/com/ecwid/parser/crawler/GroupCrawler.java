package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.NoClauseProcessCrawler;
import org.springframework.stereotype.Component;

@Component
public class GroupCrawler extends NoClauseProcessCrawler {

    public GroupCrawler() {
        super((query, fragment) -> query.getGroupings().add(fragment));
    }
}
