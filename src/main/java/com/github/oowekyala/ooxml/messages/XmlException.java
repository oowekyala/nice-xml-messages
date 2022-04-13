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

import com.github.oowekyala.ooxml.messages.Annots.Nullable;

/**
 * Generic XML exception wrapper. Can occur during validation or parsing.
 */
public final class XmlException extends RuntimeException {

    private final XmlPosition position;
    private final String simpleMessage;
    private final @Nullable String kind;
    private final XmlSeverity severity;


    /**
     * Create a new exception from the given spec.
     *
     * @param spec        A non-null spec
     * @param fullMessage Used as the {@link #toString()}. If null,
     *                    the spec's {@link NiceXmlMessageSpec#getSimpleMessage()} is used.
     * @throws NullPointerException If the spec is null
     */
    public XmlException(NiceXmlMessageSpec spec,
                        @Nullable String fullMessage) {

        super(fullMessage == null ? spec.getSimpleMessage() : fullMessage, spec.getCause());

        assert spec.getSeverity() != null;
        assert spec.getPosition() != null;

        this.position = spec.getPosition();
        this.simpleMessage = spec.getSimpleMessage();
        this.kind = spec.getKind();
        this.severity = spec.getSeverity();
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
    public @Nullable String getKind() {
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


}
