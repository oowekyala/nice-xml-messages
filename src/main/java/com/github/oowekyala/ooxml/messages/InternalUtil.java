/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.github.oowekyala.ooxml.messages;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.github.oowekyala.ooxml.messages.Annots.OneBased;


final class InternalUtil {

    static final Object[] EMPTY_OBJ_ARRAY = new Object[0];
    private static final char CARET = '^';
    private static final char SPACE = ' ';


    private InternalUtil() {

    }


    static void assertParamNotNull(String paramName, Object value) {
        if (value == null) {
            throw new NullPointerException(paramName + " should not be null");
        }
    }


    public static String buildCaretLine(String message, @OneBased int column, int rangeLen) {
        StringBuilder builder = new StringBuilder();
        repeatChar(builder, SPACE, column);
        repeatChar(builder, CARET, max(rangeLen, 1));
        return builder.append(SPACE).append(message).toString();
    }

    private static void repeatChar(StringBuilder builder, char c, int n) {
        final int start = builder.length();
        while (n > 0) {
            int lenAdded = builder.length() - start;
            if (n >= lenAdded && lenAdded > 0) {
                builder.append(builder, start, builder.length());
                n -= lenAdded;
            } else {
                builder.append(c);
                n--;
            }
        }
    }

    static String enquote(String it) {return "'" + it + "'";}

    /**
     * Tries to retrieve the position where the given exception occurred.
     * This is a best-effort approach, trying several known exception types
     * (eg {@link SAXParseException}, {@link TransformerException}).
     */
    public static XmlPosition extractPosition(Throwable throwable) {

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
     * Creates an entry for the given exception. Tries to recover the
     * position from the exception.
     *
     * @param kind      Kind of message
     * @param useColors Use terminal colors to format the message
     * @param exception Exception
     *
     * @return An exception, possibly enriched with context information
     */
    public static XmlException createEntryBestEffort(XmlPositioner positioner,
                                                     XmlMessageKind kind,
                                                     Severity severity,
                                                     boolean useColors,
                                                     Throwable exception) {

        XmlPosition pos = InternalUtil.extractPosition(exception);

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


    static class TeeReader extends FilterReader {

        private final Writer copySink;

        TeeReader(Reader source, Writer sink) {
            super(source);
            this.copySink = sink;
        }


        @Override
        public int read() throws IOException {
            int result = super.read();
            this.copySink.write(result);
            return result;
        }

        @Override
        public int read(char[] b, int off, int len) throws IOException {
            int numRead = super.read(b, off, len);
            if (numRead > 0) {
                this.copySink.write(b, off, numRead); // pay attention to use "numRead" and not "len"
            }
            return numRead;
        }

        @Override
        public int read(char[] b) throws IOException {
            int result = super.read(b);
            this.copySink.write(b);
            return result;
        }
    }
}
