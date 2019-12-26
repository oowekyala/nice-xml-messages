package com.github.oowekyala.ooxml.messages;

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

    FilePosition getPosition() {
        return position;
    }


    public int getLine() {
        return position.getLine();
    }

    public int getColumn() {
        return position.getColumn();
    }

    public boolean isPositionDefined() {
        return position != FilePosition.UNDEFINED;
    }

    public String getPositionFile() {
        return position.getFileUrlOrWhatever();
    }


    /** Returns the message kind. */
    public MessageKind getKind() {
        return message.getKind();
    }

    @Override
    public String toString() {
        String url = getPosition().getFileUrlOrWhatever();
        return message.getKind().getHeader(url) + System.lineSeparator() + message;
    }
}
