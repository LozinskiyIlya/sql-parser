package com.ecwid.parser;

import com.ecwid.parser.config.ParserApplicationConfig;
import com.ecwid.parser.service.SqlParser;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqlParserApplication {

    private static final Logger logger = Logger.getLogger(SqlParserApplication.class.getName());

    static {
        logger.setLevel(Level.INFO);
    }

    public static void main(String[] args) {
        final var context = new AnnotationConfigApplicationContext(ParserApplicationConfig.class);
        final var sqlParser = context.getBean(SqlParser.class);
        try (PushbackReader reader = new PushbackReader(new InputStreamReader(System.in))) {
            logger.log(Level.INFO, "Enter SQL query:");
            final var query = sqlParser.parse(reader);
            logger.log(Level.INFO, query.toString());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while reading SQL query", e);
        }
    }
}
