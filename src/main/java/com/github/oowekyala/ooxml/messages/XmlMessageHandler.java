package com.github.oowekyala.ooxml.messages;

import java.util.function.Consumer;

import com.github.oowekyala.ooxml.messages.more.PrintStreamMessageHandler;

/**
 * Handles XML messages, for example forwarding them to a print stream.
 */
public interface XmlMessageHandler extends Consumer<XmlException> {

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
    @Override
    void accept(XmlException entry);

}
