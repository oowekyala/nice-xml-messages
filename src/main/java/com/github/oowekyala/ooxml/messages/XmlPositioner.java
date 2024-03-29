package com.github.oowekyala.ooxml.messages;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;
import com.github.oowekyala.ooxml.messages.TextDoc.MessageTextBuilder;
import com.github.oowekyala.ooxml.messages.XmlException.Severity;

/**
 * Associates XML nodes with a position. This is a low-level utility,
 * created by this library (see {@link XmlMessageUtils#parse(DocumentBuilder, InputSource, XmlMessageHandler)
 * XmlErrorUtils::parse}).
 * It's meant as a back-end for a validation helper, like {@link XmlErrorReporter}.
 */
public interface XmlPositioner {

    /**
     * Returns an object describing the position in the file of the
     * given XML node. If no position is available, or if the parameter
     * is null, returns {@linkplain XmlPosition#isUndefined() an undefined position}.
     *
     * @param node XML node
     * @return A position
     */
    XmlPosition startPositionOf(@Nullable Node node);


    /**
     * Enrich the given message with the context of the position.
     * Typically this adds the source lines of the source file around
     * the error message.
     *
     * @param position      Position of the error
     * @param useAnsiColors Whether to use ANSI escape sequences to color the message
     * @param kind          Kind of error
     * @param severity      Severity of the message
     * @param message       Error message
     * @return The full message
     */
    String makePositionedMessage(
        XmlPosition position,
        boolean useAnsiColors,
        XmlMessageKind kind,
        XmlException.Severity severity,
        String message
    );


    /**
     * A positioner that returns undefined positions.
     *
     * @param systemId Optional system id, if it is known (but not line/columns)
     * @return A new positioner
     */
    static XmlPositioner noPositioner(@Nullable String systemId) {
        return new XmlPositioner() {

            @Override
            public XmlPosition startPositionOf(@Nullable Node node) {
                return XmlPosition.undefinedIn(systemId);
            }


            @Override
            public String makePositionedMessage(XmlPosition position, boolean useAnsiColors, XmlMessageKind kind, Severity severity, String message) {
                return withShortMessages().makePositionedMessage(position, useAnsiColors, kind, severity, message);
            }
        };
    }


    /**
     * Decorates this positioner, so that the messages are shorter.
     *
     * @return A new positioner
     */
    default XmlPositioner withShortMessages() {
        return new XmlPositioner() {
            @Override
            public XmlPosition startPositionOf(@Nullable Node node) {
                return XmlPositioner.this.startPositionOf(node);
            }


            @Override
            public String makePositionedMessage(XmlPosition position, boolean useAnsiColors, XmlMessageKind kind, Severity severity, String message) {

                String header = severity.toString();
                if (useAnsiColors) {
                    header = severity.withColor(header);
                }
                String url = position.getSystemId();
                if (!position.isUndefined() && url == null) {
                    url = "(unknown file)";
                }

                if (url != null) {
                    header += " at " + url;
                    if (!position.isUndefined()) {
                        header += ":" + position.getLine() + ":" + position.getColumn();
                    }
                }

                header += " - " + message;
                return header;
            }
        };
    }

}
