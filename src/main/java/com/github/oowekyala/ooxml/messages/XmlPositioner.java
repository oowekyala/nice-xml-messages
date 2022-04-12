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

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;

/**
 * Associates XML nodes with a position. This is a low-level utility,
 * created by this library (see {@link OoxmlFacade#parse(DocumentBuilder, InputSource) XmlErrorUtils::parse}).
 * It's meant as a back-end for a validation helper, like {@link XmlErrorReporter}.
 */
public interface XmlPositioner {

    /**
     * Returns an object describing the position in the file of the
     * given XML node. If no position is available, or if the parameter
     * is null, returns {@linkplain XmlPosition#isUndefined() an undefined position}.
     *
     * @param node XML node
     * @return A position
     */
    XmlPosition startPositionOf(@Nullable Node node);


    /**
     * Enrich the given message with the context of the position.
     * Typically this adds the source lines of the source file around
     * the error message.
     *
     * @param position        Position of the error
     * @param numContextLines Number of lines to include before and after
     * @return The full message
     */
    @Nullable ContextLines getLinesAround(
        XmlPosition position,
        int numContextLines
    );

    /**
     * A positioner that returns undefined positions.
     *
     * @param systemId Optional system id, if it is known (but not line/columns)
     * @return A new positioner
     */
    static XmlPositioner noPositioner(@Nullable String systemId) {
        return new XmlPositioner() {

            @Override
            public @Nullable ContextLines getLinesAround(XmlPosition position, int numContextLines) {
                return null;
            }

            @Override
            public XmlPosition startPositionOf(@Nullable Node node) {
                return XmlPosition.undefinedIn(systemId);
            }


        };
    }


}
