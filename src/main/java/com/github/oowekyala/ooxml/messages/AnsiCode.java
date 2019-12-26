package com.github.oowekyala.ooxml.messages;

enum AnsiCode {
    COL_GREEN("\\e[32m"),
    COL_RED("\\e[31m"),
    COL_YELLOW("\\e[33;1m"),
    ;

    private static final String RESET = "\\e[0m";
    private final String s;


    AnsiCode(String s) {
        this.s = s;
    }

    public String apply(String r) {
        return s + r + RESET;
    }
}
