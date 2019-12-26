package com.github.oowekyala.ooxml.messages;

import com.github.oowekyala.ooxml.messages.MessagePrinter.AnsiCode;

/**
 * Kind of message.
 */
public enum MessageKind {
    VALIDATION_WARNING("XML validation warning", AnsiCode.COL_YELLOW),
    VALIDATION_ERROR("XML validation error", AnsiCode.COL_RED),
    PARSING_WARNING("XML validation warning", AnsiCode.COL_YELLOW),
    PARSING_ERROR("XML parsing error", AnsiCode.COL_RED);

    private final String template;
    private final AnsiCode color;

    MessageKind(String s, AnsiCode color) {
        template = s;
        this.color = color;
    }

    String addColor(String str) {
        return color.apply(str);
    }

    AnsiCode getColor() {
        return color;
    }

    /**
     *
     * @param fileLoc
     * @return
     */
    public String getHeader(/*Nullable*/String fileLoc) {
        return fileLoc == null ? template : template + " in " + fileLoc;
    }
}
