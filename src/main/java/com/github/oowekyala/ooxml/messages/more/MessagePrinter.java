package com.github.oowekyala.ooxml.messages.more;

/**
 * Outputs messages in some way.
 */
public interface MessagePrinter {

    /**
     * Outputs messages to {@link System#err}, with colors enabled.
     */
    MessagePrinter SYSTEM_ERR = new PrintStreamMessagePrinter(System.out, System.err, true);

    /** Outputs messages to {@link System#err}, with no colors. */
    MessagePrinter SYSTEM_ERR_NO_COLORS = new PrintStreamMessagePrinter(System.out, System.err, false);


    void error(String msg);


    void warn(String message);


    void info(String message);


    boolean supportsAnsiColors();

}
