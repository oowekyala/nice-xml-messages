package com.github.oowekyala.ooxml.messages;

import com.github.oowekyala.ooxml.messages.XmlException.Severity;

/**
 * Handles XML messages, for example forwarding them to a print stream.
 */
public interface XmlMessageHandler {

    /**
     * Outputs messages to {@link System#err}, with colors enabled, and
     * debug off.
     */
    XmlMessageHandler SYSTEM_ERR = new PrintStreamMessageHandler(true, false);


    /**
     * Returns true if error messages may be colored with
     * {@linkplain TerminalColor ANSI escape sequences}.
     */
    boolean supportsAnsiColors();


    /**
     * Handle an XML message. May throw, ignore, or print
     * to an external stream.
     *
     * @param entry Message to handle
     */
    default void accept(XmlException entry) {
        printMessageLn(entry.getKind(), entry.getSeverity(), entry.toString());
    }


    void printMessageLn(XmlMessageKind kind, Severity severity, String message);

}
