package com.ecwid.parser;

import com.ecwid.parser.config.ParserApplicationConfig;
import com.ecwid.parser.service.SqlParser;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SqlParserApplication {
    public static final String EXIT = "q";

    public static void main(String[] args) {
        final var context = new AnnotationConfigApplicationContext(ParserApplicationConfig.class);
        final var sqlParser = context.getBean(SqlParser.class);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                printInstructions();
                final var sql = readSqlQuery(reader);
                if (sql == null) {
                    System.out.println("Bye!");
                    break;
                }
                System.out.println("You entered: \n" + sql);
                final var query = sqlParser.parse(sql);
                System.out.println("\nParsed query:\n" + QueryPrinter.printJson(query));
                drainReader(reader);
            }
        } catch (IOException e) {
            System.err.printf("Error while reading input: %s\n", e.getMessage());
        }
    }

    private static void printInstructions() {
        System.out.println("Enter a query to parse:");
        System.out.println(" - End the query with ';' to start parsing");
        System.out.println(" - Type 'q' to exit");
    }

    private static String readSqlQuery(BufferedReader reader) throws IOException {
        StringBuilder sqlBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            // Check if the user wants to exit
            if (line.trim().equalsIgnoreCase(EXIT)) {
                return null;
            }

            sqlBuilder.append(line).append(" ");
            if (line.trim().endsWith(";")) {
                break;
            }
        }

        return sqlBuilder.toString().trim();
    }

    private static void drainReader(BufferedReader reader) throws IOException {
        while (reader.ready()) {
            reader.readLine();
        }
    }
}
