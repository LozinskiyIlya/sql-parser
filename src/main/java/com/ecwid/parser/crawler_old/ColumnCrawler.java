package com.ecwid.parser.crawler_old;

import com.ecwid.parser.fragment.enity.Column;
import com.ecwid.parser.fragment.enity.Query;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.ecwid.parser.Lexemes.*;

@Component
public class ColumnCrawler extends SectionAwareCrawler {

    @Override
    public void crawl(Query query, String select, Supplier<String> nextFragmentSupplier) {
        String nextFragment;
        final var columns = query.getColumns();
        while ((nextFragment = nextFragmentSupplier.get()) != null) {
            if (shouldDelegate(nextFragment)) {
                delegate(query, nextFragment, nextFragmentSupplier);
                return;
            }
            if (LEX_COMMA.equals(nextFragment)) {
                continue;
            }
            if (LEX_OPEN_BRACKET.equals(nextFragment)) {
                // it's a function call
                final var functionBuilder = new StringBuilder(nextFragment);
                if (!columns.isEmpty()) {
                    // the last inserted column was a function name
                    functionBuilder.insert(0, columns.removeLast().getName());
                }
                crawlUntilAndReturnNext(LEX_CLOSE_BRACKET::equals, functionBuilder::append, nextFragmentSupplier);
                functionBuilder.append(LEX_CLOSE_BRACKET);
                nextFragment = functionBuilder.toString();
            }
            columns.add(new Column(nextFragment, null));
        }
    }
}
