package com.ecwid.parser.crawler.base;

import com.ecwid.parser.fragment.Constant;
import com.ecwid.parser.fragment.Query;
import org.springframework.util.StringUtils;

import java.util.function.BiConsumer;

public abstract class IntegerCrawler extends NoClauseProcessCrawler {

    public IntegerCrawler(BiConsumer<Query, Integer> onInteger, String clause) {
        super((query, fragment) -> {
            if (fragment instanceof Constant) {
                final var value = ((Constant) fragment).getValue();
                if (StringUtils.hasText(value) && value.chars().allMatch(Character::isDigit)) {
                    onInteger.accept(query, Integer.parseInt(value));
                    return;
                }
            }
            throw new IllegalArgumentException(clause + " should be an integer");
        });
    }
}
