package com.github.oowekyala.ooxml.messages;

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

    /**
     * Add a color relevant to this kind to the given string. This uses
     * ANSI escape sequences.
     *
     * @param toColor String to surround with escape sequences
     *
     * @return The string, prefixed with an ANSI color, and suffixed
     *     with {@value TerminalColor#ANSI_RESET}
     */
    public String withColor(String toColor) {
        return toColor;
    }

    public String toString() {
        return displayName;
    }
}
