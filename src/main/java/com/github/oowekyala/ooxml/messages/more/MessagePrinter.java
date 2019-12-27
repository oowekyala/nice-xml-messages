package com.github.oowekyala.ooxml.messages.more;

import com.github.oowekyala.ooxml.messages.XmlException;
import com.github.oowekyala.ooxml.messages.XmlMessageHandler;

/**
 * Outputs messages in some way.
 */
public interface MessagePrinter {

    /**
     * Outputs messages to {@link System#err}, with colors enabled, and
     * debug off.
     */
    MessagePrinter SYSTEM_ERR = new PrintStreamMessagePrinter(true, false);


    void error(String msg);


    void warn(String message);


    void info(String message);


    default void debug(String message) {

    }


    boolean supportsAnsiColors();


    default XmlMessageHandler asMessageHandler() {
        return new XmlMessageHandler() {

            MessagePrinter printer = MessagePrinter.this;

            @Override
            public boolean supportsAnsiColors() {
                return printer.supportsAnsiColors();
            }

            @Override
            public void accept(XmlException entry) {
                switch (entry.getSeverity()) {
                case DEBUG:
                    printer.debug(entry.toString());
                    break;
                case INFO:
                    printer.info(entry.toString());
                    break;
                case WARNING:
                    printer.warn(entry.toString());
                    break;
                case ERROR:
                    printer.error(entry.toString());
                    break;
                case FATAL:
                    throw entry;
                }
            }
        };
    }
}
