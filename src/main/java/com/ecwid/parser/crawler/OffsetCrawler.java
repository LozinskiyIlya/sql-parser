package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Constant;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.domain.Fragment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

@Component
public class OffsetCrawler extends FragmentCrawler {

    @Override
    protected void addFragmentToQuery(Query query, Fragment fragment) {
        if (fragment instanceof Constant) {
            final var value = ((Constant) fragment).getValue();
            if (StringUtils.hasText(value) && value.chars().allMatch(Character::isDigit)) {
                query.setOffset(Integer.parseInt(value));
                return;
            }
        }
        throw new IllegalArgumentException("OFFSET should be an integer");
    }

    @Override
    protected String addClauseToQueryAndReturnNextLex(Query query, String currentSection, Supplier<String> nextLex) {
        return nextLex.get();
    }
}
