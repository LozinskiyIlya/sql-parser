package com.ecwid.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

import static com.ecwid.parser.Lexemes.*;


public class SqlParser {

    public Query parse(String sql) {
        return parse(readerFromString(sql));
    }

    public Query parse(BufferedReader bufferedReader) {
        try (BufferedReader reader = bufferedReader) {
            return parseQuery(reader);
        } catch (IOException e) {
            System.out.println("Error reading input");
        }
        return null;
    }

    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        System.out.println("Type next query");
        final var sqlParser = new SqlParser();
        sqlParser.parse(reader);
    }

    private static Query parseQuery(BufferedReader reader) {
        String lex = nextLex(reader);
        if (!LEX_SELECT.equals(lex)) {
            System.out.println("Expected SELECT command, but got " + lex);
            return null;
        }
        Stack<String> commandStack = new Stack<>();
        commandStack.push(lex);
        while ((lex = nextLex(reader)) != null) {
            if (COMMANDS.contains(lex)) {
                printStack(commandStack);
                commandStack.clear();
            }
            commandStack.push(lex);
        }
        printStack(commandStack);
        System.out.println();
        return null;
    }

    private static String nextLex(BufferedReader reader) {
        int character;
        try {
            final var lex = new StringBuilder();
            while ((character = reader.read()) != -1) {
                char c = (char) character;
                if (SEPARATORS.contains(String.valueOf(c))) {
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
        } catch (IOException e) {
            System.out.println("Error reading input");
            e.printStackTrace();
        }
        return null;
    }

    private static void printStack(Stack<String> stack) {
        System.out.println("Stack:");
        stack.forEach(System.out::println);
        System.out.println();
    }

    private static BufferedReader readerFromString(String s) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
    }
}
