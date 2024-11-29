package com.ecwid.parser;

import com.ecwid.parser.fragments.Query;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

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

        String command = "select";
        final var parameters = new LinkedList<String>();
        final var query = new Query();

        while ((lex = nextLex(reader)) != null) {
            if (lex.isEmpty()) {
                continue;
            }
            if (LEX_SEMICOLON.equals(lex)) {
                break;
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

    private static void addQueryFragment(Query query, String command, List<String> parameters) {
        System.out.println("Command: " + command);
        System.out.println("Parameters: " + parameters);
    }

    private static void skipJoinKeywordIfNeeded(String currentLex, PushbackReader reader) throws IOException {
        if (JOINS.contains(currentLex)) {
            final var shouldBeJoin = nextLex(reader);
            if (!LEX_JOIN.equals(shouldBeJoin)) {
                throw new IllegalStateException("JOIN keyword expected");
            }
        }
    }

    private static String nextLex(PushbackReader reader) throws IOException {
        int character;
        final var lex = new StringBuilder();
        while ((character = reader.read()) != -1) {
            final var currentChar = (char) character;
            final var currentCharAsString = String.valueOf(currentChar);

            if (LEX_SINGLE_QUOTE.equals(currentCharAsString)) {
                return readStringToTheEnd(reader);
            }
            if (LEX_COMMA.equals(currentCharAsString)) {
                continue;
            }
            if (SEPARATORS.contains(currentCharAsString)) {
                if (lex.isEmpty()) {
                    return currentCharAsString;
                }
                reader.unread(currentChar);
                return lex.toString().toLowerCase();
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

    private static BufferedReader readerFromString(String s) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
    }
}
