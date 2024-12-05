package com.ecwid.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("When join includes")
public class JoinParserIT extends AbstractSpringParserTest {

    @Nested
    @DisplayName("basic")
    class Basic{

        @Test
        @DisplayName("INNER JOIN")
        void innerJoin() throws Exception {
            final var sql = "SELECT * FROM table1 INNER JOIN table2 ON table1.id = table2.id;";
            final var parsed = sqlParser.parse(sql);
            assertEquals(1, parsed.getJoins().size());
            final var join = parsed.getJoins().getFirst();
//            assertJoinEquals(Join.JoinType.INNER_JOIN, "table2", "table1.id", "table2.id", join);
        }
    }
}
