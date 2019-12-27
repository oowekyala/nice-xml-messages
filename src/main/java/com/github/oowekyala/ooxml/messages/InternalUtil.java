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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXParseException;


final class InternalUtil {

    static final Object[] EMPTY_OBJ_ARRAY = new Object[0];

    private InternalUtil() {

    }

    public static String addNSpacesLeft(String s, int n) {
        StringBuilder builder = new StringBuilder();
        builder.append(' ');
        repeatChar(builder, ' ', n - 1);
        builder.append(s);
        return builder.toString();
    }

    // input builder must not be empty
    private static void repeatChar(StringBuilder builder, char c, int n) {
        while (n > 0) {
            if (n < builder.length()) {
                builder.append(c);
                n--;
            } else {
                int len = builder.length();
                builder.append(builder);
                n -= len;
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

    /*
        Those are here to clarify between 1 and 0 based offsets.
     */

}
