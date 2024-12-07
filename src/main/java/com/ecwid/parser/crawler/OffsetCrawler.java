package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.NoClauseProcessCrawler;
import com.ecwid.parser.fragment.Constant;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OffsetCrawler extends NoClauseProcessCrawler {

    public OffsetCrawler() {
        super((query, fragment) -> {
            if (fragment instanceof Constant) {
                final var value = ((Constant) fragment).getValue();
                if (StringUtils.hasText(value) && value.chars().allMatch(Character::isDigit)) {
                    query.setOffset(Integer.parseInt(value));
                    return;
                }
            }
            throw new IllegalArgumentException("OFFSET should be an integer");
        });
    }
}
