package com.ecwid.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

import static com.ecwid.parser.Lexemes.*;


public class SqlParser {

    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        System.out.println("Type next query");
        String lex = nextLex();
        if (!LEX_SELECT.equals(lex)) {
            System.out.println("Expected SELECT command, but got " + lex);
            return;
        }
        Stack<String> commandStack = new Stack<>();
        commandStack.push(lex);
        while ((lex = nextLex()) != null) {
            if (COMMANDS.contains(lex)) {
                printStack(commandStack);
                commandStack.clear();
            }
            commandStack.push(lex);
        }
        printStack(commandStack);
        System.out.println();
    }

    private static void printStack(Stack<String> stack) {
        System.out.println("Stack:");
        stack.forEach(System.out::println);
        System.out.println();
    }

    private static String nextLex() {
        int character;
        try {
            final var lex = new StringBuilder();
            while ((character = reader.read()) != -1) {
                char c = (char) character;
                if (Character.isWhitespace(c)) {
                    if (lex.isEmpty()) {
                        continue;
                    }
                    return lex.toString().toLowerCase();
                }
                lex.append(c);
                if (SEPARATORS.contains(lex.toString())) {
                    return lex.toString();
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading input");
            e.printStackTrace();
        }
        return null;
    }


    private static Query parse() {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading input");
            e.printStackTrace();
        }
        return null;
    }
}
