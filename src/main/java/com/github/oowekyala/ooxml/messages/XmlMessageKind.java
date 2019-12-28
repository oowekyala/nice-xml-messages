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
