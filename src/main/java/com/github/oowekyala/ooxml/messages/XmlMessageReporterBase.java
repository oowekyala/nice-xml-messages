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

import org.w3c.dom.Node;

/**
 * Base implementation of {@link XmlMessageReporter}.
 */
public abstract class XmlMessageReporterBase<M> implements XmlMessageReporter<M> {

    protected final XmlPositioner positioner;
    protected final OoxmlFacade ooxml;


    protected XmlMessageReporterBase(OoxmlFacade ooxml, XmlPositioner positioner) {
        this.positioner = positioner;
        this.ooxml = ooxml;
    }


    /**
     * Creates the object returned by {@link #at(Node)}. Override
     * this instead of {@link #at(Node)} because maybe in the future
     * other {@link #at(Node)} overloads will be provided.
     *
     * @param position   Position of the message
     * @param positioner Positioner
     */
    protected abstract M create2ndStage(XmlPosition position, XmlPositioner positioner);


    /**
     * Handle an XML exception. The default just calls the printer.
     */
    protected void handleEx(XmlException e) {
        ooxml.getPrinter().accept(e);
    }


    @Override
    public M at(Node node) {
        return create2ndStage(
            positioner.startPositionOf(node),
            positioner
        );
    }


    /**
     * Do nothing by default.
     */
    @Override
    @SuppressWarnings("RedundantThrows")
    public void close() {

    }
}
