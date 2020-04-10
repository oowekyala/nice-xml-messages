package com.github.oowekyala.ooxml.messages;


import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class MessageUtil {

    static String enquote(String it) {return "'" + it + "'";}


    /**
     * Tries to retrieve the position where the given exception occurred. This is a best-effort approach, trying several
     * known exception types (eg {@link SAXParseException}, {@link TransformerException}).
     */
    static XmlPosition extractPosition(Throwable throwable) {

        if (throwable instanceof XmlException) {
            return ((XmlException) throwable).getPosition();
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


    /**
     * Creates an entry for the given exception. Tries to recover the position from the exception.
     *
     * @param kind      Kind of message
     * @param useColors Use terminal colors to format the message
     * @param exception Exception
     * @return An exception, possibly enriched with context information
     */
    static XmlException createEntryBestEffort(XmlPositioner positioner,
                                              XmlMessageKind kind,
                                              XmlException.Severity severity,
                                              boolean useColors,
                                              Throwable exception) {

        XmlPosition pos = extractPosition(exception);

        final String simpleMessage;
        if (exception instanceof TransformerException
            && exception.getCause() instanceof SAXException) {
            simpleMessage = exception.getCause().getMessage();
        } else {
            simpleMessage = exception.getMessage();
        }


        if (pos.isUndefined()) {
            // unknown exception
            return new XmlException(pos,
                                    kind.getHeader(severity) + "\n" + simpleMessage,
                                    simpleMessage,
                                    kind,
                                    severity,
                                    exception);

        } else {
            String fullMessage = positioner.makePositionedMessage(pos, useColors, kind, severity, simpleMessage);
            return new XmlException(pos, fullMessage, simpleMessage, kind, severity, exception);
        }
    }


    static String readFully(Reader reader) throws IOException {

        StringWriter writer = new StringWriter();
        char[] buf = new char[1024 * 8];
        int read = reader.read(buf);

        while (read >= 0) {
            writer.write(buf, 0, read);
            read = reader.read(buf);
        }

        return writer.toString();
    }
}
