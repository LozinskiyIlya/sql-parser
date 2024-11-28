package com.ecwid.parser;

import com.ecwid.parser.fragments.Query;

import java.io.*;
import java.util.Stack;

import static com.ecwid.parser.Lexemes.*;


public class SqlParser {

    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {
        System.out.println("Type next query");
        final var sqlParser = new SqlParser();
        final var query = sqlParser.parse(reader);
        System.out.println(query);
    }

    public Query parse(String sql) throws IOException {
        return parse(readerFromString(sql));
    }

    public Query parse(BufferedReader bufferedReader) throws IOException {
        System.out.println("Parsing query...");
        try (final var reader = new PushbackReader(bufferedReader)) {
            final var query = parseQuery(reader);
            System.out.println("Query parsed");
            return query;
        }
    }

    private static Query parseQuery(PushbackReader reader) throws IOException, IllegalStateException {
        String lex = nextLex(reader);
        if (!LEX_SELECT.equals(lex)) {
            throw new IllegalStateException("Query must start with SELECT");
        }
        Stack<String> commandStack = new Stack<>();
        Query query = new Query();
        commandStack.push(lex);
        while ((lex = nextLex(reader)) != null) {
            if (LEX_SEMICOLON.equals(lex)) {
                break;
            }
            if (COMMANDS.contains(lex)) {
                addNodeFromStack(query, commandStack);
            }
            commandStack.push(lex);

            if (JOINS.contains(lex)) {
                commandStack.push(nextLex(reader));
            }
        }
        addNodeFromStack(query, commandStack);
        System.out.println();
        return query;
    }

    private static String nextLex(PushbackReader reader) throws IOException {
        int character;
        final var lex = new StringBuilder();
        while ((character = reader.read()) != -1) {
            char c = (char) character;
            if (LEX_SINGLE_QUOTE.equals(String.valueOf(c))) {
                return readStringToTheEnd(reader);
            }
            if (SEPARATORS.contains(String.valueOf(c))) {
                if (lex.isEmpty()) {
                    return String.valueOf(c);
                }
                reader.unread(c);
                return lex.toString().toLowerCase();
            }
            if (Character.isWhitespace(c)) {
                if (lex.isEmpty()) {
                    continue;
                }
                return lex.toString().toLowerCase();
            }
            lex.append(c);
        }
        return null;
    }

    private static String readStringToTheEnd(PushbackReader reader) throws IOException, IllegalStateException {
        int character;
        final var lex = new StringBuilder(LEX_SINGLE_QUOTE);
        while ((character = reader.read()) != -1) {
            char c = (char) character;
            lex.append(c);
            if (LEX_SINGLE_QUOTE.equals(String.valueOf(c))) {
                return lex.toString();
            }
        }
        throw new IllegalStateException("String is not closed");
    }

    private static void addNodeFromStack(Query query, Stack<String> stack) {
        while (!stack.isEmpty()) {
        }
    }

    private static BufferedReader readerFromString(String s) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
    }
}
