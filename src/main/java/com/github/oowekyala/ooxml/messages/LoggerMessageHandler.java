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

import java.util.logging.Logger;

import com.github.oowekyala.ooxml.messages.XmlException.XmlSeverity;

/**
 * Implements {@link XmlMessageHandler} with a {@link Logger}
 * as back-end.
 */
public class LoggerMessageHandler implements XmlMessageHandler {

    private final Logger logger;
    private final boolean supportsColor;


    public LoggerMessageHandler(Logger logger, boolean supportsColor) {
        this.logger = logger;
        this.supportsColor = supportsColor;
    }


    @Override
    public boolean supportsAnsiColors() {
        return supportsColor;
    }


    @Override
    public void printMessageLn(XmlMessageKind kind, XmlSeverity severity, String message) {
        logger.log(severity.toJutilLevel(), message);

    }
}
