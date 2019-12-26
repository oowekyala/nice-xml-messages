package com.github.oowekyala.ooxml.messages;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

/**
 * Associates XML nodes with a position.
 */
public interface XmlPositioner {

    /**
     * Returns an object describing the position in the file of the
     * given XML node. If no position is available, returns
     * {@link XmlPosition#UNDEFINED}.
     */
    XmlPosition startPositionOf(Node node);


    String makePositionedMessage(
        XmlPosition position,
        boolean supportsAnsiColors,
        XmlMessageKind kind,
        String message
    );


    /**
     * Tries to retrieve the position where the given exception occurred.
     * This is a best-effort approach, trying several known exception types
     * (eg {@link SAXParseException}, {@link TransformerException}).
     */
    static XmlPosition extractPosition(Throwable throwable) {

        if (throwable instanceof XmlParseException) {
            return ((XmlParseException) throwable).getPosition();
        } else if (throwable instanceof SAXParseException) {
            SAXParseException e = (SAXParseException) throwable;
            return new XmlPosition(e.getSystemId(), e.getLineNumber(), e.getColumnNumber());
        } else if (throwable instanceof TransformerException) {
            if (throwable.getCause() instanceof SAXParseException) {
                return extractPosition(throwable.getCause());
            }

            SourceLocator locator = ((TransformerException) throwable).getLocator();
            if (locator != null) {
                return new XmlPosition(locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
            }
        }

        return XmlPosition.UNDEFINED;
    }

}
