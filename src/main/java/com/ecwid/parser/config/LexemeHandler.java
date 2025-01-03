package com.ecwid.parser.config;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface LexemeHandler {
    String[] lexemes();
}
