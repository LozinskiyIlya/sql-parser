package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Join;
import com.ecwid.parser.fragment.domain.Column;
import com.ecwid.parser.fragment.domain.NameAliasPair;
import com.ecwid.parser.fragment.domain.Query;
import com.ecwid.parser.fragment.domain.Table;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.fragment.Join.JoinType.joinTypeFullLexemes;

@Component
public class JoinCrawler extends SectionAwareCrawler {
    @Override
    public void crawl(Query query, String currentSection, Supplier<String> fragments) {
        final var join = new Join();
        final var tableFirstFragment = crawlForJoinType(join, currentSection, fragments);
        final var leftColumnFirstFragment = crawlForTable(join, tableFirstFragment, fragments);
        final var shouldBeEqualSign = crawlForColumn(join, leftColumnFirstFragment, fragments);
        if (!LEX_EQUALS.equals(shouldBeEqualSign)) {
            throw new IllegalArgumentException("Expected = sign, but got " + shouldBeEqualSign);
        }
        final var nextSection = crawlForColumn(join, fragments.get(), fragments);
        query.getJoins().add(join);
        delegate(query, nextSection, fragments);
    }

    private String crawlForJoinType(Join join, String firstFragment, Supplier<String> fragments) {
        final var joinTypeParts = new LinkedList<String>();
        joinTypeParts.add(firstFragment);
        final var nextFragment = crawlUntilAndReturnNext(
                fragment -> {
                    if (joinTypeParts.contains(LEX_JOIN)) {
                        return true;
                    }
                    joinTypeParts.add(fragment);
                    return false;
                },
                fragment -> {
                },
                fragments);
        join.setType(joinTypeFullLexemes.get(String.join(LEX_SPACE, joinTypeParts)));
        return nextFragment;
    }

    private String crawlForTable(Join join, String tableName, Supplier<String> fragments) {
        final var pair = new NameAliasPair();
        pair.push(tableName);
        crawlUntilAndReturnNext(
                LEX_ON::equals,
                pair::push,
                fragments);
        final var table = new Table(pair);
        join.setTable(table);
        return fragments.get();
    }

    private boolean isJoinType(List<String> parts) {
        return joinTypeFullLexemes.containsKey(String.join(LEX_SPACE, parts));
    }

    private String crawlForColumn(Join join, String firstFragment, Supplier<String> fragments) {
        final var columnBuilder = new StringBuilder(firstFragment);
        final var nextFragment = crawlUntilAndReturnNext(
                fragment -> LEX_EQUALS.equals(fragment) || shouldDelegate(fragment),
                columnBuilder::append,
                fragments);
        final var column = new Column(columnBuilder.toString(), null);
        if (join.getLeftColumn() == null) {
            join.setLeftColumn(column);
        } else {
            join.setRightColumn(column);
        }
        return nextFragment;
    }
}
