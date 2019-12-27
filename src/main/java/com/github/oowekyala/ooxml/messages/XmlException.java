package com.github.oowekyala.ooxml.messages;

/**
 * Generic XML exception wrapper. Can occur during validation or parsing.
 */
public class XmlException extends RuntimeException {

    private final XmlPosition position;
    private final String simpleMessage;
    private final XmlMessageKind kind;

    public XmlException(XmlPosition position,
                        String fullMessage,
                        String simpleMessage,
                        XmlMessageKind kind) {
        this(position, fullMessage, simpleMessage, kind, null);
    }

    public XmlException(XmlPosition position,
                        String fullMessage,
                        String simpleMessage,
                        XmlMessageKind kind,
                        Throwable cause) {
        super(fullMessage, cause);
        this.position = position;
        this.simpleMessage = simpleMessage;
        this.kind = kind;
    }


    public String getSimpleMessage() {
        return simpleMessage;
    }


  public XmlPosition getPosition() {
      return position;
  }


    /** Returns the message kind. */
    public XmlMessageKind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
