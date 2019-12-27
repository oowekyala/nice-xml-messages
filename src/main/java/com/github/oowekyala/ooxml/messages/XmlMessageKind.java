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
     * Add a color relevant to this kind to the given string. This uses
     * ANSI escape sequences.
     *
     * @param toColor String to surround with escape sequences
     *
     * @return The string, prefixed with an ANSI color, and suffixed
     *     with {@value TerminalColor#ANSI_RESET}
     */
    String withColor(String toColor);


    /**
     * Gets the descriptive header for this kind. This is what's displayed
     * before the exception description in full messages, for example "XML parsing error".
     *
     * @return The header for messages of this kind
     */
    String getHeader();


    /**
     * Basic message kinds.
     */
    enum StdMessageKind implements XmlMessageKind {
        /**
         * A warning reported after parsing, by {@link XmlErrorReporter#warn(Node, String, Object...)
         * XmlErrorReporter::warn}.
         */
        VALIDATION_WARNING("XML validation warning", TerminalColor.COL_YELLOW),

        /**
         * An error reported after parsing, by {@link XmlErrorReporter#error(Node, Throwable) XmlErrorReporter::error}.
         */
        VALIDATION_ERROR("XML validation error", TerminalColor.COL_RED),

        /** A warning reported by the XML parser. */
        PARSING_WARNING("XML validation warning", TerminalColor.COL_YELLOW),

        /**
         * An error thrown by the XML parser, this occurs when the XML
         * is not well-formed or otherwise invalid. The error may be
         * recoverable.
         */
        PARSING_ERROR("XML parsing error", TerminalColor.COL_RED);

        private final String header;
        private final TerminalColor color;

        StdMessageKind(String s, TerminalColor color) {
            header = s;
            this.color = color;
        }

        @Override
        public String withColor(String toColor) {
            return color.apply(toColor, false, false, false);
        }


        @Override
        public String getHeader() {
            return header;
        }
    }
}
