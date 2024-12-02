package com.ecwid.parser.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PushbackReader;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.Lexemes.LEX_SINGLE_QUOTE;

@Service
public class LexemeReader {

    public String nextLex(PushbackReader reader) {
        int character;
        final var lex = new StringBuilder();
        try {
            while ((character = reader.read()) != -1) {
                final var currentChar = (char) character;
                final var currentCharAsString = String.valueOf(currentChar);
                if (LEX_SINGLE_QUOTE.equals(currentCharAsString)) {
                    return readStringToTheEnd(reader);
                }
                if (SEPARATORS.contains(currentCharAsString)) {
                    if (lex.isEmpty()) {
                        return currentCharAsString;
                    }
                    reader.unread(currentChar);
                    return lex.toString().toLowerCase();
                }
                if (Character.isWhitespace(currentChar) || LEX_SEMICOLON.equals(currentCharAsString)) {
                    if (lex.isEmpty()) {
                        continue;
                    }
                    return lex.toString().toLowerCase();
                }
                lex.append(currentChar);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lex.isEmpty() ? null : lex.toString().toLowerCase();
    }

    private String readStringToTheEnd(PushbackReader reader) throws IOException, IllegalStateException {
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
}
