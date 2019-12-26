package com.github.oowekyala.ooxml.messages;

/**
 * Outputs messages in some way.
 */
public interface MessagePrinter {

    MessagePrinter SYSTEM_ERR = new PrintStreamMessagePrinter(System.out, System.err, true);

    MessagePrinter DEFAULT = new PrintStreamMessagePrinter(System.out, System.err, false);


    void error(String msg);


    void warn(String message);


    void info(String message);


    default String applyAnsi(AnsiCode color, String string) {
        return color.apply(string);
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
