package com.ecwid.parser.crawler.base.helper;

import com.ecwid.parser.fragment.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
@AllArgsConstructor
public class CrawlContext {
    Query query;
    String currentSection;
    Supplier<String> nextLexSupplier;
    int openBrackets;
}