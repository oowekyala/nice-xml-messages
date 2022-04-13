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

/**
 * Severity of a message.
 */
public enum XmlSeverity {
    /**
     * A warning, with a yellow color.
     */
    WARNING("Warning") {
        @Override
        public String withColor(String toColor) {
            return TerminalColor.COL_YELLOW.apply(toColor, false, false, false);
        }
    },
    /**
     * An error, with a red color.
     */
    ERROR("Error") {
        @Override
        public String withColor(String toColor) {
            return TerminalColor.COL_RED.apply(toColor, false, false, false);
        }
    };

    private final String displayName;


    XmlSeverity(String displayName) {
        this.displayName = displayName;
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
