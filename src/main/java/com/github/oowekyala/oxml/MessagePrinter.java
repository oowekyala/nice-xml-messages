package com.github.oowekyala.oxml;

/**
 * @author Clément Fournier
 */
public interface MessagePrinter {

    MessagePrinter DEFAULT = new MessagePrinter() {};


    default boolean supportsAnsiColor() {
        return true;
    }

    default void error(String msg) {
        System.err.println("\n[error] " + msg);
    }


    default void warn(String message) {
        System.err.println("\n[warning] " + message);
    }


    default void println(String message) {
        System.out.println("[info] " + message);
    }

}
