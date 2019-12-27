package com.github.oowekyala.ooxml.messages;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.github.oowekyala.ooxml.messages.more.XmlErrorReporter;

/**
 * Associates XML nodes with a position. This is a low-level utility,
 * created by this library. It's meant as a back-end for a validation
 * helper, for which {@link XmlErrorReporter} provides an example.
 */
public interface XmlPositioner {

    /**
     * Returns an object describing the position in the file of the
     * given XML node. If no position is available, returns
     * {@link XmlPosition#UNDEFINED}.
     */
    // TODO support attribute nodes
    XmlPosition startPositionOf(Node node);


    /**
     * Enrich the given [message] with the context of the [position].
     * Typically this adds the source lines of the source file around
     * the error message.
     *
     * @param position           Position of the error
     * @param supportsAnsiColors Whether to use ANSI escape sequences to color the message
     * @param kind               Kind of error
     * @param message            Error message
     *
     * @return The full message
     */
    String makePositionedMessage(
        XmlPosition position,
        boolean supportsAnsiColors,
        XmlMessageKind kind,
        String message
    );


    default XmlException createEntry(Node node,
                                     XmlMessageKind kind,
                                     boolean useColors,
                                     String message,
                                     Object... args) {

        XmlPosition pos = startPositionOf(node);
        String simpleMessage = String.format(message, args);
        String fullMessage = makePositionedMessage(pos, useColors, kind, simpleMessage);

        return new XmlException(pos, fullMessage, simpleMessage, kind, null);
    }


    default XmlException createEntry(Node node, XmlMessageKind kind, boolean useColors, Throwable exception) {
        XmlPosition pos = startPositionOf(node);
        String simpleMessage = exception.getMessage();
        String fullMessage = makePositionedMessage(pos, useColors, kind, simpleMessage);

        return new XmlException(pos, fullMessage, simpleMessage, kind, exception);
    }


    default XmlException createEntry(XmlMessageKind kind, boolean useColors, Throwable exception) {
        XmlPosition pos = InternalUtil.extractPosition(exception);

        final String simpleMessage;
        if (exception instanceof TransformerException
            && exception.getCause() instanceof SAXException) {
            simpleMessage = exception.getCause().getMessage();
        } else {
            simpleMessage = exception.getMessage();
        }


        if (pos.equals(XmlPosition.UNDEFINED)) {
            // unknown exception
            return new XmlException(XmlPosition.UNDEFINED,
                                    kind.getHeader() + "\n" + simpleMessage,
                                    simpleMessage,
                                    kind,
                                    exception);

        } else {
            String fullMessage = makePositionedMessage(pos, useColors, kind, simpleMessage);
            return new XmlException(pos, fullMessage, simpleMessage, kind, exception);
        }
    }


}
