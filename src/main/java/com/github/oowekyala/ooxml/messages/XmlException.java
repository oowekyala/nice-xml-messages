package com.github.oowekyala.ooxml.messages;

/**
 * Generic XML exception wrapper. Can occur during validation or parsing.
 */
public final class XmlException extends RuntimeException {

    private final XmlPosition position;
    private final String simpleMessage;
    private final XmlMessageKind kind;
    private final Severity severity;

    public XmlException(XmlPosition position,
                        String fullMessage,
                        String simpleMessage,
                        XmlMessageKind kind,
                        Severity severity) {
        this(position, fullMessage, simpleMessage, kind, severity, null);
    }

    public XmlException(XmlPosition position,
                        String fullMessage,
                        String simpleMessage,
                        XmlMessageKind kind,
                        Severity severity,
                        Throwable cause) {

        super(fullMessage, cause);

        assert severity != null;
        assert kind != null;
        assert position != null;

        this.position = position;
        this.simpleMessage = simpleMessage;
        this.kind = kind;
        this.severity = severity;
    }

    /**
     * Returns the error message, without the surrounding line context.
     * {@link #getMessage()} will return a fuller message.
     */
    public String getSimpleMessage() {
        return simpleMessage;
    }


    /**
     * Returns the position where the error occurred. This may
     * be {@linkplain XmlPosition#isUndefined() undefined}.
     */
    public XmlPosition getPosition() {
        return position;
    }


    /**
     * Returns the message kind.
     */
    public XmlMessageKind getKind() {
        return kind;
    }

    /**
     * Returns the severity of the message.
     */
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
