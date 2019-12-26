package com.github.oowekyala.ooxml;

import com.github.oowekyala.ooxml.ErrorReporter.Message;

/**
 * Generic XML exception wrapper. Can occur during validation or parsing.
 */
public class XmlParseException extends RuntimeException {

    private final Position position;
    private final Message message;

    XmlParseException(Position position, Message message) {
        super(message.toString());
        this.position = position;
        this.message = message;
    }

    XmlParseException(Position position, Message message, Throwable cause) {
        super(message.toString(), cause);
        this.position = position;
        this.message = message;
    }

    public Position getPosition() {
        return position;
    }


    public Message getMessageObj() {
        return message;
    }

    @Override
    public String toString() {
        String url = getPosition().getFileUrlOrWhatever();
        return message.getKind().getHeader(url) + System.lineSeparator() + message;
    }
}
