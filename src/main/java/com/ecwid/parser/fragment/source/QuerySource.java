package com.ecwid.parser.fragment.source;

import com.ecwid.parser.fragment.Query;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuerySource implements Source {
    private Query query;
}
