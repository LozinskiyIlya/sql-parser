package com.ecwid.parser.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PushbackReader;

import static com.ecwid.parser.Lexemes.*;
import static com.ecwid.parser.Lexemes.LEX_SINGLE_QUOTE;

@Service
public class LexemeReader {

    public String nextLex(PushbackReader reader) {
        int c;
        final var lex = new StringBuilder();
        try {
            while ((c = reader.read()) != -1) {
                final var currentChar = (char) c;
                final var currentCharAsString = String.valueOf(currentChar);
                if (SEPARATORS.contains(currentCharAsString)) {
                    if (lex.isEmpty()) {
                        if (LEX_SINGLE_QUOTE.equals(currentCharAsString)) {
                            return readStringToTheEnd(lex, reader);
                        }
                        return currentCharAsString;
                    } else if (LEX_OPEN_BRACKET.equals(currentCharAsString)) {
                        return readFunctionToTheEnd(lex, reader);
                    }
                    reader.unread(c);
                    break;
                }
                if (Character.isWhitespace(c)) {
                    if (lex.isEmpty()) {
                        continue;
                    }
                    break;
                }
                lex.append(currentChar);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lex.isEmpty() ? null : lex.toString().toLowerCase();
    }

    private String readStringToTheEnd(StringBuilder quotedString, PushbackReader reader) throws IOException, IllegalStateException {
        int character;
        quotedString.append(LEX_SINGLE_QUOTE);
        while ((character = reader.read()) != -1) {
            char c = (char) character;
            quotedString.append(c);
            if (LEX_SINGLE_QUOTE.equals(String.valueOf(c))) {
                return quotedString.toString();
            }
        }
        throw new IllegalStateException("String is not closed");
    }

    private String readFunctionToTheEnd(StringBuilder signature, PushbackReader reader) throws IOException, IllegalStateException {
        int character;
        int brackets = 1;
        signature.append(LEX_OPEN_BRACKET);
        while ((character = reader.read()) != -1) {
            char c = (char) character;
            signature.append(c);
            if (LEX_CLOSE_BRACKET.equals(String.valueOf(c))) {
                brackets--;
                if (brackets == 0) {
                    return signature.toString().toLowerCase();
                }
            }
            if (LEX_OPEN_BRACKET.equals(String.valueOf(c))) {
                brackets++;
            }
        }
        throw new IllegalStateException("Function is not closed");
    }
}
