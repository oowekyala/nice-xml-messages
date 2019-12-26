package com.github.oowekyala.ooxml.messages;

import com.github.oowekyala.ooxml.messages.ErrorReporter.Message;

/**
 * Generic XML exception wrapper. Can occur during validation or parsing.
 */
public class XmlParseException extends RuntimeException {

    private final FilePosition position;
    private final Message message;

    XmlParseException(FilePosition position, Message message) {
        super(message.toString());
        this.position = position;
        this.message = message;
    }

    XmlParseException(FilePosition position, Message message, Throwable cause) {
        super(message.toString(), cause);
        this.position = position;
        this.message = message;
    }

    public FilePosition getPosition() {
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
