package com.ecwid.parser.service;

import com.ecwid.parser.AbstractSpringParserTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.ecwid.parser.service.SqlParser.readerFromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexemeReaderTest extends AbstractSpringParserTest {

    @Autowired
    private LexemeReader lexemeReader;

    @TestFactory
    @DisplayName("Should extract lexemes")
    Stream<DynamicTest> shouldHandleVariousInputs() {
        return Stream.of(
                new TestCase("is empty", "", List.of()),
                new TestCase("is whitespace", " ", List.of()),
                new TestCase("has whitespace", " abc ", List.of("abc")),
                new TestCase("has different whitespaces", "\n\t\r", List.of()),
                new TestCase("is newline", "\n", List.of()),
                new TestCase("has newline", "a\nb", List.of("a", "b")),
                new TestCase("is quoted string", "'quoted text'", List.of("'quoted text'")),
                new TestCase("has quoted string", "abc'abc(,);'xyz", List.of("abc", "'abc(,);'", "xyz")),
                new TestCase("is semicolon", ";", List.of(";")),
                new TestCase("has semicolon", "a; b", List.of("a", ";", "b")),
                new TestCase("is escaped char", "\\", List.of("\\")),
                new TestCase("has escaped char", "ab\\cd", List.of("ab\\cd")),
                new TestCase("is open bracket", "(", List.of("(")),
                new TestCase("has open bracket", "a(b", List.of("a", "(", "b")),
                new TestCase("is close bracket", ")", List.of(")")),
                new TestCase("has close bracket", "a)b", List.of("a", ")", "b")),
                new TestCase("has open and close brackets", "count(*) b, a", List.of("count","(", "*", ")", "b", ",", "a")),
                new TestCase("is a list of values", "a b c", List.of("a", "b", "c")),
                new TestCase("is a list of values with quotes", "a 'b c'", List.of("a", "'b c'")),
                new TestCase("is csv", "a,b,c", List.of("a", ",", "b", ",", "c")),
                new TestCase("is csv with quotes", "a, 'b, c'", List.of("a", ",", "'b, c'")),
                new TestCase("is a list of mixed values", "a, (a, 'b', c(a.d), e), 'e'",
                        List.of("a", ",", "(", "a", ",", "'b'", ",", "c", "(", "a.d", ")", ",", "e", ")", ",", "'e'"))
                ).map(testCase -> DynamicTest.dynamicTest(
                "when input " + testCase.displayName(),
                () -> assertInputProduces(testCase.input(), testCase.expected())
        ));
    }

    private void assertInputProduces(String input, List<String> expected) {
        final var reader = readerFromString(input);
        final List<String> lexemes = new ArrayList<>();
        String lexeme;

        while ((lexeme = lexemeReader.nextLex(reader)) != null) {
            lexemes.add(lexeme);
        }

        assertEquals(expected, lexemes);
    }

    private record TestCase(String displayName, String input, List<String> expected) {
    }
}
