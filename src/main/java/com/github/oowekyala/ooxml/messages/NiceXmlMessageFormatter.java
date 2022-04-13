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
 * Formats a {@link NiceXmlMessageSpec} to make the full message
 * of an {@link XmlException}.
 *
 * @see OoxmlFacade#withFormatter(NiceXmlMessageFormatter)
 */
public interface NiceXmlMessageFormatter {

    String formatSpec(OoxmlFacade ooxml, NiceXmlMessageSpec spec, XmlPositioner positioner);


    NiceXmlMessageFormatter SINGLE_LINE =
        (ooxml, spec, positioner) -> MessageUtil.headerOnly(spec, spec.getSimpleMessage(), true);


    /**
     * Formats errors like:
     * <pre>{@code
     * Error (XML parsing) at /some/file.xml:3:15
     *     1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
     *     2| <list>
     *     3|     <list foo="&amb;"/>
     *                            ^ The entity "amb" was referenced, but not declared.
     *
     *     4| </list>
     * }</pre>
     */
    NiceXmlMessageFormatter FULL_MESSAGE = (ooxml, spec, positioner) -> {
        @Nullable ContextLines linesAround =
            positioner.getLinesAround(spec.getPosition(), spec.getNumLinesAround());

        return linesAround == null ? SINGLE_LINE.formatSpec(ooxml, spec, positioner)
                                   : linesAround.make(ooxml, spec);
    };

}
