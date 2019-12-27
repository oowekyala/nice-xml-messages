package com.github.oowekyala.ooxml.messages;

import javax.management.modelmbean.XMLParseException;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.more.XmlErrorReporter;

/**
 * Kind of a message (or {@linkplain XMLParseException exception}),
 * mostly just a formatting helper.
 */
public interface XmlMessageKind {


    /**
     * Gets the descriptive header for this kind. This is what's displayed
     * before the exception description in full messages, for example "XML parsing error".
     *
     * @return The header for messages of this kind
     */
    String getHeader(Severity severity);


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
        SCHEMA_VALIDATION("XML validation"),

        ;

        private final String header;

        StdMessageKind(String s) {
            header = s;
        }


        @Override
        public String getHeader(Severity severity) {
            return severity.toString() + " (" + header + ")";
        }
    }
}
