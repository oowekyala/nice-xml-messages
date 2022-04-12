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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.oowekyala.ooxml.messages.Annots.OneBased;
import com.github.oowekyala.ooxml.messages.Annots.ZeroBased;

/**
 * Helper object.
 */
public class ContextLines {

    /**
     * Line number of the first line of the list in the real document
     */
    private final @OneBased int first;
    /**
     * Index in the list of the line that has the error.
     */
    private final @ZeroBased int errorIdx;
    private final List<String> lines;


    ContextLines(List<String> lines, @OneBased int first, int errorIdx) {
        this.lines = lines;
        this.first = first;
        this.errorIdx = errorIdx;
        assert (0 <= errorIdx && errorIdx < lines.size())
            : "Weird indices --- first=" + first + ", errorIdx=" + errorIdx + ", lines=" + lines;
    }


    String make(NiceXmlMessageSpec spec) {

        int pad = stringLengthOf(lines.size() + first);

        List<String> withLineNums = IntStream.range(0, lines.size())
                                             .mapToObj(i -> addLineNum(i, pad))
                                             .collect(Collectors.collectingAndThen(Collectors.toList(), ArrayList::new));

        String errorLine = addLineNum(errorIdx, pad);
        // diff added by line numbers
        int offset = errorLine.length() - lines.get(errorIdx).length();

        String messageLine = InternalUtil.buildCaretLine(spec.getSimpleMessage().trim(),
                                                         spec.getPosition().getColumn() + offset - 1,
                                                         spec.getPosition().getLength());

        String colored = spec.isUseAnsiColors() ? spec.getSeverity().withColor(messageLine) : messageLine;

        withLineNums.add(errorIdx + 1, colored);
        withLineNums.add(errorIdx + 2, ""); // skip a line


        return MessageUtil.headerOnly(spec, String.join("\n", withLineNums), false);
    }


    private int stringLengthOf(int i) {
        return (i + "").length();
    }


    private String addLineNum(@ZeroBased int idx, int pad) {
        return String.format(" %" + pad + "d| %s", 1 + idx + first, lines.get(idx));
    }


}
