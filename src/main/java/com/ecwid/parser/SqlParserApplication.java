package com.ecwid.parser;

import com.ecwid.parser.config.ParserApplicationConfig;
import com.ecwid.parser.service.SqlParser;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;

public class SqlParserApplication {
    public static final int EXIT_CHAR_CODE = 113; // 'q'

    public static void main(String[] args) {
        final var context = new AnnotationConfigApplicationContext(ParserApplicationConfig.class);
        final var sqlParser = context.getBean(SqlParser.class);
        try (PushbackReader reader = new PushbackReader(new InputStreamReader(System.in))) {
            while (true) {
                printInstructions();
                skipWhiteSpaces(reader);
                checkExit(reader);
                final var query = sqlParser.parse(reader);
                System.out.println("\nParsed query:\n" + QueryPrinter.printJson(query));
            }
        } catch (IOException e) {
            System.err.printf("Error while reading input %s\n", e.getMessage());
        }
    }

    private static void printInstructions() {
        System.out.println("Enter a query to parse");
        System.out.println(" - type ';' to start parsing");
        System.out.println(" - type 'q' to finish");
    }

    private static void skipWhiteSpaces(PushbackReader reader) throws IOException {
        int c;
        while ((c = reader.read()) != -1) {
            if (!Character.isWhitespace(c)) {
                reader.unread(c);
                break;
            }
        }
    }

    private static void checkExit(PushbackReader reader) throws IOException {
        int c = reader.read();
        if (c == EXIT_CHAR_CODE) {
            System.out.println("Bye!");
            System.exit(0);
        }
        reader.unread(c);
    }
}
