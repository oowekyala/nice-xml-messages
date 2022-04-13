/*
 * MIT License
 *
 * Copyright (c) 2022 Clément Fournier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.oowekyala.ooxml.messages;


import static com.github.oowekyala.ooxml.messages.ErrorCleaner.isSchemaValidationMessage;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;

class MessageUtil {


    private static final String KIND_SCHEMA_VALIDATION = "Schema validation";
    private static final String KIND_PARSING = "XML parsing";


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
     * @param exception Exception
     * @return An exception, possibly enriched with context information
     */
    static XmlException createEntryBestEffort(OoxmlFacade ooxml,
                                              XmlPositioner positioner,
                                              XmlSeverity severity,
                                              Throwable exception) {

        String kind = extractKind(exception);
        XmlPosition pos = extractPosition(exception);
        String simpleMessage = extractSimpleMessage(exception);

        NiceXmlMessageSpec spec = new NiceXmlMessageSpec(pos, simpleMessage)
            .withKind(kind)
            .withSeverity(severity)
            .withCause(exception);

        String fullMessage = ooxml.getFormatter().formatSpec(ooxml, spec, positioner);
        return new XmlException(spec, fullMessage);
    }


    private static String extractKind(Throwable exception) {
        return exception instanceof SAXParseException && isSchemaValidationMessage(exception.getMessage())
               ? KIND_SCHEMA_VALIDATION : KIND_PARSING;
    }

    private static String extractSimpleMessage(Throwable exception) {
        final String simpleMessage;
        if (exception instanceof TransformerException
            && exception.getCause() instanceof SAXException) {
            simpleMessage = exception.getCause().getMessage();
        } else {
            simpleMessage = exception.getMessage();
        }
        return simpleMessage;
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


    public static String headerOnly(NiceXmlMessageSpec spec, String message, boolean singleLine) {

        @Nullable String url = spec.getPosition().getSystemId();

        String header = spec.getSeverity().toString();
        String kind = spec.getKind();
        if (kind != null) {
            header += " (" + kind + ")";
        }
        if (url != null) {
            if (spec.getPosition().isUndefined()) {
                header += " in " + url;
            } else {
                header += " at " + url + ":" + spec.getPosition().getLine() + ":" + spec.getPosition().getColumn();
            }
        }

        if (singleLine) {
            return header + " - " + message;
        } else {
            return header + "\n" + message;
        }
    }
}
