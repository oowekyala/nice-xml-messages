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
 * @author Clément Fournier
 */
class PartialFilePositioner implements XmlPositioner {

    private static final int NUM_LINES_AROUND = 3;
    protected final TextDoc textDoc;
    private final String systemId;


    /**
     * @param fullFileText Full text of the XML file
     */
    public PartialFilePositioner(String fullFileText, String systemId) {
        this.textDoc = new TextDoc(fullFileText);
        this.systemId = systemId;
    }


    @Override
    public XmlPosition startPositionOf(@Nullable Node node) {
        return XmlPosition.undefinedIn(systemId);
    }


    @Override
    public @Nullable ContextLines getLinesAround(XmlPosition position, int numContextLines) {
        return position.isUndefined() ? null : textDoc.getLinesAround(position.getLine(), numContextLines);
    }


}
