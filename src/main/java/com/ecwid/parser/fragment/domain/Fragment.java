package com.ecwid.parser.fragment.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Fragment {

    @JsonIgnore
    Object getValue();
}
