package com.github.oowekyala.ooxml.messages;

import javax.management.modelmbean.XMLParseException;

/**
 * Kind of a message (or {@linkplain XMLParseException exception}),
 * mostly just a formatting helper.
 *
 * @author Clément Fournier
 */
public interface XmlMessageKind {

    String withColor(String toColor);


    String getHeader();


    /**
     * Basic message kinds.
     */
    enum StdMessageKind implements XmlMessageKind {
        VALIDATION_WARNING("XML validation warning", AnsiCode.COL_YELLOW),
        VALIDATION_ERROR("XML validation error", AnsiCode.COL_RED),
        PARSING_WARNING("XML validation warning", AnsiCode.COL_YELLOW),
        PARSING_ERROR("XML parsing error", AnsiCode.COL_RED);

        private final String header;
        private final AnsiCode color;

        StdMessageKind(String s, AnsiCode color) {
            header = s;
            this.color = color;
        }

        @Override
        public String withColor(String toColor) {
            return color.apply(toColor);
        }


        @Override
        public String getHeader() {
            return header;
        }
    }
}
