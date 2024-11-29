package com.ecwid.parser;

import com.ecwid.parser.fragments.Query;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import static com.ecwid.parser.Lexemes.*;


public class SqlParser {


    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Type next query");
            final var sqlParser = new SqlParser();
            final var query = sqlParser.parse(reader);
            System.out.println(query);
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    public Query parse(String sql) throws IOException {
        return parse(readerFromString(sql));
    }

    public Query parse(BufferedReader reader) throws IOException {
        System.out.println("Parsing query...");
        final var query = parseQuery(reader);
        System.out.println("Query parsed");
        return query;
    }

    private static Query parseQuery(BufferedReader reader) throws IOException, IllegalStateException {
        String lex = nextLex(reader);
        if (!LEX_SELECT.equals(lex)) {
            throw new IllegalStateException("Query must start with SELECT");
        }

        String command = "select";
        final var parameters = new LinkedList<String>();
        final var query = new Query();

        while ((lex = nextLex(reader)) != null && !LEX_SEMICOLON.equals(lex)) {
            if (lex.isEmpty()) {
                continue;
            }
            if (COMMANDS.contains(lex)) {
                addQueryFragment(query, command, parameters);
                skipJoinKeywordIfNeeded(lex, reader);
                parameters.clear();
                command = lex;
                continue;
            }
            parameters.add(lex);
        }
        addQueryFragment(query, command, parameters);
        System.out.println();
        return query;
    }

    private static void skipJoinKeywordIfNeeded(String currentLex, BufferedReader reader) throws IOException {
        if (JOINS.contains(currentLex)) {
            final var shouldBeJoin = nextLex(reader);
            if (!LEX_JOIN.equals(shouldBeJoin)) {
                throw new IllegalStateException("JOIN keyword expected");
            }
        }
    }

    private static String nextLex(BufferedReader reader) throws IOException {
        int character;
        final var lex = new StringBuilder();
        while ((character = reader.read()) != -1) {
            final var currentChar = (char) character;
            final var currentCharAsString = String.valueOf(currentChar);
            if (LEX_SINGLE_QUOTE.equals(currentCharAsString)) {
                return readStringToTheEnd(reader);
            }
            if (SEPARATORS.contains(currentCharAsString)) {
                continue;
            }
            if (Character.isWhitespace(currentChar)) {
                if (lex.isEmpty()) {
                    continue;
                }
                return lex.toString().toLowerCase();
            }
            lex.append(currentChar);
        }
        return null;
    }

    private static String readStringToTheEnd(BufferedReader reader) throws IOException, IllegalStateException {
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

    private static BufferedReader readerFromString(String s) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
    }

    private static void addQueryFragment(Query query, String command, List<String> parameters) {
        System.out.println("Command: " + command);
        System.out.println("Parameters: " + parameters);
    }
}
