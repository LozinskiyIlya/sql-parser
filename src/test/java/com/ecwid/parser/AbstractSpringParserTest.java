package com.ecwid.parser;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(SqlParser.class)
@DisplayName("Should parse SQL")
public abstract class AbstractSpringParserTest {
    protected final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected SqlParser sqlParser;
}
