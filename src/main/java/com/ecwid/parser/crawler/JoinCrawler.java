package com.ecwid.parser.crawler;

import com.ecwid.parser.crawler.base.SectionAwareCrawler;
import com.ecwid.parser.fragment.Join;
import com.ecwid.parser.fragment.NameAliasPair;
import com.ecwid.parser.fragment.Query;
import com.ecwid.parser.fragment.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.fragment.Join.JoinType.joinTypeFullLexemes;

@Component
@RequiredArgsConstructor
public class JoinCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String currentSection, Supplier<String> fragments) {
        final var join = new Join();
        query.getJoins().add(join);
        final var tableFirstFragment = crawlForJoinType(join, currentSection, fragments);
        crawlForTable(join, tableFirstFragment, fragments);
        delegate(query, LEX_ON, fragments);
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

    private void crawlForTable(Join join, String tableName, Supplier<String> fragments) {
        final var pair = new NameAliasPair();
        pair.push(tableName);
        crawlUntilAndReturnNext(
                LEX_ON::equals,
                pair::push,
                fragments);
        final var table = new Table(pair);
        join.setTable(table);
    }
}
