package com.ecwid.parser;

import com.ecwid.parser.config.ParserApplicationConfig;
import com.ecwid.parser.service.SqlParser;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;

public class SqlParserApplication {

    public static void main(String[] args) {
        final var context = new AnnotationConfigApplicationContext(ParserApplicationConfig.class);
        final var sqlParser = context.getBean(SqlParser.class);
        try (PushbackReader reader = new PushbackReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("Enter a query to parse");
                System.out.println(" - type 'q' to finish");
                System.out.println(" - use ';' as a separator\n");

                int c = reader.read();
                if (c == 113) {
                    // 'q'
                    System.out.println("Bye!");
                    break;
                }
                reader.unread(c);
                final var query = sqlParser.parse(reader);
                System.out.println("Parsed query:\n" + QueryPrinter.printJson(query));
            }
        } catch (IOException e) {
            System.err.printf("Error while reading input %s\n", e.getMessage());
        }
    }
}
