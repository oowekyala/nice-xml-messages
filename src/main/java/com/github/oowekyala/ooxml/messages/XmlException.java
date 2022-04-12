/*
 * MIT License
 *
 * Copyright (c) 2022 Clément Fournier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.oowekyala.ooxml.messages;

import java.util.logging.Level;

/**
 * Generic XML exception wrapper. Can occur during validation or parsing.
 */
public final class XmlException extends RuntimeException {

    private final XmlPosition position;
    private final String simpleMessage;
    private final XmlMessageKind kind;
    private final XmlSeverity severity;

    public XmlException(XmlPosition position,
                        String fullMessage,
                        String simpleMessage,
                        XmlMessageKind kind,
                        XmlSeverity severity) {
        this(position, fullMessage, simpleMessage, kind, severity, null);
    }

    public XmlException(XmlPosition position,
                        String fullMessage,
                        String simpleMessage,
                        XmlMessageKind kind,
                        XmlSeverity severity,
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
    public XmlSeverity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return getMessage();
    }


    /**
     * Severity of a message.
     */
    public enum XmlSeverity {
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


        XmlSeverity(String displayName) {
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


        XmlSeverity fromJutilLevel(Level level) {
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
