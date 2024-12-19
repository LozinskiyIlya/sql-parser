package com.ecwid.parser.config;

import com.ecwid.parser.crawler.base.Crawler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ComponentScan(basePackages = "com.ecwid.parser")
@RequiredArgsConstructor
public class ParserApplicationConfig {

    private final LexemeHandlerBeanPostProcessor lexemeHandlerBeanPostProcessor;

    @Bean
    public Map<String, Crawler> sectionAgainstCrawler() {
        return lexemeHandlerBeanPostProcessor.getCrawlersMap();
    }

}
