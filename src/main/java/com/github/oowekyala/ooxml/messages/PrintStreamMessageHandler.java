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

import java.io.PrintStream;

import com.github.oowekyala.ooxml.messages.XmlException.XmlSeverity;

/**
 * Implements {@link XmlMessageHandler} with a pair of {@link PrintStream}s.
 */
public class PrintStreamMessageHandler implements XmlMessageHandler {

    private final PrintStream out;
    private final PrintStream err;
    private final boolean supportsColor;
    private final boolean debugEnabled;

    public PrintStreamMessageHandler(PrintStream out,
                                     PrintStream err,
                                     boolean supportsColor,
                                     boolean debugEnabled) {
        this.out = out;
        this.err = err;
        this.supportsColor = supportsColor;
        this.debugEnabled = debugEnabled;
    }

    public PrintStreamMessageHandler(boolean supportsColor, boolean debugEnabled) {
        this(System.out, System.err, supportsColor, debugEnabled);
    }

    @Override
    public boolean supportsAnsiColors() {
        return supportsColor;
    }


    @Override
    public void printMessageLn(XmlMessageKind kind, XmlSeverity severity, String message) {
        switch (severity) {
        case INFO:
            out.println(message);
            break;
        case DEBUG:
            if (!debugEnabled) {
                break;
            }
        case WARNING:
        case ERROR:
        case FATAL:
            err.println(message);
        }
    }
}
