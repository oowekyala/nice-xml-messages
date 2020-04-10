package com.github.oowekyala.ooxml.messages;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;

/**
 * Associates XML nodes with a position. This is a low-level utility,
 * created by this library (see {@link XmlErrorUtils#parse(DocumentBuilder, InputSource, XmlMessageHandler) XmlErrorUtils::parse}).
 * It's meant as a back-end for a validation helper, like {@link XmlErrorReporter}.
 */
public interface XmlPositioner {

    /**
     * Returns an object describing the position in the file of the
     * given XML node. If no position is available, returns
     * {@linkplain XmlPosition#isUndefined() an undefined position}.
     *
     * @param node XML node
     *
     * @return A position
     */
    XmlPosition startPositionOf(@Nullable Node node);


    /**
     * Enrich the given message with the context of the position.
     * Typically this adds the source lines of the source file around
     * the error message.
     *
     * @param position           Position of the error
     * @param supportsAnsiColors Whether to use ANSI escape sequences to color the message
     * @param kind               Kind of error
     * @param severity           Severity of the message
     * @param message            Error message
     *
     * @return The full message
     */
    String makePositionedMessage(
        XmlPosition position,
        boolean supportsAnsiColors,
        XmlMessageKind kind,
        XmlException.Severity severity,
        String message
    );


}
