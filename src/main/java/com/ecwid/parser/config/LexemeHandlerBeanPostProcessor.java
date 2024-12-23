package com.ecwid.parser.config;

import com.ecwid.parser.crawler.base.Crawler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ecwid.parser.Lexemes.LEX_EMPTY;

@Getter
@Component
public class LexemeHandlerBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, Crawler> crawlersMap = new HashMap<>();

    /**
     * @noinspection NullableProblems
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        LexemeHandler annotation = getAnnotation(bean);
        if (annotation != null) {
            for (var lex : annotation.lexemes()) {
                crawlersMap.put(lex, (Crawler) bean);
                // additional case for Finisher.class which should be triggered on null as well
                if (lex.equals(LEX_EMPTY)) {
                    crawlersMap.put(null, (Crawler) bean);
                }
            }
        }

        return bean;
    }

    @SneakyThrows
    private LexemeHandler getAnnotation(Object bean) {
        if (bean instanceof Advised) {
            bean = ((Advised) bean).getTargetSource().getTarget();
        }
        return bean.getClass().getAnnotation(LexemeHandler.class);
    }
}
