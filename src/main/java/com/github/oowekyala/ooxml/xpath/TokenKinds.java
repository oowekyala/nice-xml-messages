package com.github.oowekyala.ooxml.xpath;

/**
 *
 */
public enum TokenKinds {
    IDENTIFIER,

    PATH,
    PATH_DOUBLE,

    LBRACKET,
    RBRACKET,
    DOUBLE_COLON,
    AT,
    PERIOD,
    STAR,
    DOLLAR,

    OP,

    LIT_STRING,
    LIT_DOUBLE,
    LIT_DECIMAL,
    LIT_INTEGER,

    AXIS,
    NODE_KIND,

    IGNORED,
    EOF,
}
