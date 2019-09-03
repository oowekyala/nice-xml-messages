package com.github.oowekyala.oxml;

/**
 * Outputs messages in some way.
 */
public interface MessagePrinter {

    MessagePrinter DEFAULT = new MessagePrinter() {

        @Override
        public String applyAnsi(AnsiCode color, String string) {
            return string;
        }
    };


    default void error(String msg) {
        System.err.println("\n[error] " + msg);
    }


    default void warn(String message) {
        System.err.println("\n[warning] " + message);
    }


    default String applyAnsi(AnsiCode color, String string) {
        return color.apply(string);
    }

    default void println(String message) {
        System.out.println("[info] " + message);
    }


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
}
