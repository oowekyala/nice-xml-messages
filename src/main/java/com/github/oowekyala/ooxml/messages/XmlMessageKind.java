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

import org.w3c.dom.Node;

/**
 * Kind of a {@linkplain XmlException message}, mostly just a formatting
 * helper.
 */
public interface XmlMessageKind {


    /**
     * Gets the descriptive header for this kind. This is what's displayed
     * before the exception description in full messages, for example "XML parsing error".
     *
     * @param severity Severity of the warning
     *
     * @return The header for messages of this kind
     */
    String getHeader(XmlSeverity severity);


    /**
     * Basic message kinds.
     */
    enum StdMessageKind implements XmlMessageKind {

        /**
         * An error reported after parsing, by {@link XmlErrorReporter#error(Node, Throwable) XmlErrorReporter::error}.
         */
        USER_VALIDATION("XML validation"),

        /**
         * An error thrown by the XML parser, this occurs when the XML
         * is not well-formed or otherwise invalid. The error may be
         * recoverable.
         */
        PARSING("XML parsing"),

        /** A warning reported by a schema validator. */
        SCHEMA_VALIDATION("Schema validation"),

        ;

        private final String header;

        StdMessageKind(String s) {
            header = s;
        }


        @Override
        public String getHeader(XmlSeverity severity) {
            return severity.toString() + " (" + header + ")";
        }
    }
}
