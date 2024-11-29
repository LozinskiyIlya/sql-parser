package com.ecwid.parser.crawler;

import com.ecwid.parser.fragment.Query;

import java.util.function.Supplier;

public interface Crawler {

    Crawler next();

    String myCommand();

    void addQueryFragment(Query query, String command, Supplier<String> fragmentSupplier);
}
