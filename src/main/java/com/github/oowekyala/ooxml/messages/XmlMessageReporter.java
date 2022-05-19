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

import com.github.oowekyala.ooxml.messages.Annots.Nullable;

/**
 * Reports errors in an XML document. This is meant as a helper
 * to carry around while validating an XML document. This interface
 * is the API provided to the validating code, what happens to the
 * messages is up to the implementation of the second stage (which is
 * meant to be some specific logger/ message reporter). Instances may wrap an
 * {@link XmlPositioner} to associate DOM nodes with an {@link XmlPosition position}
 * for better error messages.
 *
 * <p>A base implementation is available in {@link XmlMessageReporterBase}.
 * Implementations should use {@link OoxmlFacade#getPrinter()}
 * as a back-end to render the messages.
 */
public interface XmlMessageReporter<M> extends AutoCloseable {

    /**
     * Returns the second stage, which typically allows reporting
     * messages like {@code reporter.at(node).error("an error");}.
     */
    M at(@Nullable Node node);


    @Override
    void close();
}
