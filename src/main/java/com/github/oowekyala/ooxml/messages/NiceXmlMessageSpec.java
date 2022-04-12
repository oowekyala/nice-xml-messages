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

public class NiceXmlMessageSpec {

    private final XmlPosition position;
    private Throwable cause;
    private boolean useAnsiColors;
    private XmlMessageKind kind;
    private XmlSeverity severity;
    private String simpleMessage;
    private boolean shortMessage;
    private int numLinesAround = 2;


    /**
     * @param position      Position of the error
     * @param simpleMessage Error message
     */
    public NiceXmlMessageSpec(XmlPosition position, String simpleMessage) {
        this.position = position;
        this.simpleMessage = simpleMessage;
    }


    public XmlPosition getPosition() {
        return position;
    }


    public boolean isUseAnsiColors() {
        return useAnsiColors;
    }


    public @Nullable XmlMessageKind getKind() {
        return kind;
    }


    public XmlSeverity getSeverity() {
        return severity;
    }


    public String getSimpleMessage() {
        return simpleMessage;
    }


    public int getNumLinesAround() {
        return numLinesAround;
    }


    public boolean isShortMessage() {
        return shortMessage;
    }

    public @Nullable Throwable getCause() {
        return cause;
    }

    public NiceXmlMessageSpec withAnsiColors(boolean useAnsiColors) {
        this.useAnsiColors = useAnsiColors;
        return this;
    }


    public NiceXmlMessageSpec withKind(XmlMessageKind kind) {
        this.kind = kind;
        return this;
    }


    public NiceXmlMessageSpec withSeverity(XmlSeverity severity) {
        this.severity = severity;
        return this;
    }


    public NiceXmlMessageSpec setSimpleMessage(String simpleMessage) {
        this.simpleMessage = simpleMessage;
        return this;
    }


    public NiceXmlMessageSpec setShortMessage(boolean shortMessage) {
        this.shortMessage = shortMessage;
        return this;
    }


    public NiceXmlMessageSpec withContextLines(int numLinesAround) {
        this.numLinesAround = numLinesAround;
        return this;
    }


    public NiceXmlMessageSpec withCause(Throwable cause) {
        this.cause = cause;
        return this;
    }

}
