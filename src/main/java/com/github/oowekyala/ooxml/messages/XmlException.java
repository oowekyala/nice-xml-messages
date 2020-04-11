package com.github.oowekyala.ooxml.messages;

import java.util.logging.Level;

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


    /**
     * Severity of a message.
     */
    public enum Severity {
        DEBUG("Debug info"),
        INFO("Info"),
        WARNING("Warning") {
            @Override
            public String withColor(String toColor) {
                return TerminalColor.COL_YELLOW.apply(toColor, false, false, false);
            }
        },
        ERROR("Error") {
            @Override
            public String withColor(String toColor) {
                return TerminalColor.COL_RED.apply(toColor, false, false, false);
            }
        },
        FATAL("Fatal error") {
            @Override
            public String withColor(String toColor) {
                return TerminalColor.COL_RED.apply(toColor, false, false, true);
            }
        };

        private final String displayName;


        Severity(String displayName) {
            this.displayName = displayName;
        }


        Level toJutilLevel() {
            switch (this) {
            case INFO:
                return Level.INFO;
            case DEBUG:
                return Level.FINE;
            case WARNING:
                return Level.WARNING;
            case ERROR:
            case FATAL:
                return Level.SEVERE;
            default:
                throw new AssertionError();
            }
        }


        Severity fromJutilLevel(Level level) {
            if (level == Level.INFO || level == Level.ALL) {
                return INFO;
            } else if (level == Level.FINE) {
                return DEBUG;
            } else if (level == Level.WARNING) {
                return WARNING;
            } else if (level == Level.SEVERE) {
                return ERROR;
            }
            return DEBUG;
        }


        /**
         * Add a color relevant to this kind to the given string. This uses
         * ANSI escape sequences.
         *
         * @param toColor String to surround with escape sequences
         * @return The string, prefixed with an ANSI color, and suffixed
         * with {@value TerminalColor#ANSI_RESET}
         */
        public String withColor(String toColor) {
            return toColor;
        }

        public String toString() {
            return displayName;
        }
    }
}
