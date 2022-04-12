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

import java.util.function.Consumer;

import org.w3c.dom.Node;

/**
 * Reports errors in an XML document. This is meant as a helper
 * to carry around while validating an XML document. This interface
 * is the API provided to the validating code, what happens to the
 * messages is up to the implementation. Instances may wrap an
 * {@link XmlPositioner} to associate DOM nodes with a {@link XmlPosition position}
 * for better error messages.
 *
 * <p>A simple implementation is available in {@link DefaultXmlErrorReporter}.
 * Another implementation, {@link AccumulatingErrorReporter} holds
 * off the actual printing until the reporter is {@link #close() closed}.
 * Those implementations use {@link XmlMessageHandler} as a back-end
 * to render the messages.
 *
 * <p>The formatter used to template messages is implementation dependent,
 * as is the behaviour when the template argument array is null.
 */
public abstract class XmlMessageReporterBase<M> implements XmlMessageReporter<M> {

    private final XmlPositioner positioner;
    protected final OoxmlFacade ooxml;


    protected XmlMessageReporterBase(OoxmlFacade ooxml, XmlPositioner positioner) {
        this.positioner = positioner;
        this.ooxml = ooxml;
    }


    /**
     * @param position   Position
     * @param positioner Positioner
     * @param handleEx   Callback to be called when an {@link XmlException} is created by the second stage
     */
    protected abstract M create2ndStage(XmlPosition position,
                                        XmlPositioner positioner,
                                        Consumer<XmlException> handleEx);


    protected void handleEx(XmlException e) {
        ooxml.getPrinter().accept(e);
    }


    @Override
    public M at(Node node) {
        return create2ndStage(
            positioner.startPositionOf(node),
            positioner,
            ooxml.getPrinter()::accept
        );
    }


    /**
     * Do nothing by default.
     */
    @Override
    @SuppressWarnings("RedundantThrows")
    public void close() throws Exception {

    }
}
