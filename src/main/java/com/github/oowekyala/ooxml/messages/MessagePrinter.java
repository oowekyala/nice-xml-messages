package com.github.oowekyala.ooxml.messages;

/**
 * Outputs messages in some way.
 */
public interface MessagePrinter {

    MessagePrinter SYSTEM_ERR = new PrintStreamMessagePrinter(System.out, System.err, true);
    MessagePrinter SYSTEM_ERR_NO_COLORS = new PrintStreamMessagePrinter(System.out, System.err, false);


    void error(String msg);


    void warn(String message);


    void info(String message);


    boolean supportsAnsiColors();

}
