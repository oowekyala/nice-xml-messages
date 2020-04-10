package com.github.oowekyala.ooxml.messages;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.oowekyala.ooxml.internal_not_api.InternalUtil;
import com.github.oowekyala.ooxml.messages.Annots.OneBased;
import com.github.oowekyala.ooxml.messages.Annots.ZeroBased;

class TextDoc {


    /**
     * This list has one entry for each line, denoting the start offset of the line.
     * The start offset of the next line includes the length of the line terminator
     * (1 for \r|\n, 2 for \r\n).
     */
    private final List<Integer> lineOffsets = new ArrayList<>();
    private final List<String> lines = new ArrayList<>();
    private final int sourceCodeLength;
    private final String sourceCode;

    TextDoc(String sourceCode) {
        sourceCodeLength = sourceCode.length();
        this.sourceCode = sourceCode;

        try (Scanner scanner = new Scanner(new StringReader(sourceCode))) {
            int currentGlobalOffset = 0;

            while (scanner.hasNextLine()) {
                lineOffsets.add(currentGlobalOffset);
                currentGlobalOffset += getLineLengthWithLineSeparator(scanner);
            }
        }

        if (lines.isEmpty()) {
            // empty doc yields one empty line
            lines.add("");
        }
    }

    /** Returns the full source. */
    public String getTextString() {
        return sourceCode;
    }

    MessageTextBuilder getLinesAround(@OneBased int line, int numLinesAround) {
        @ZeroBased int zeroL = line - 1;
        @ZeroBased int firstL = Math.max(0, zeroL - numLinesAround + 1);
        @ZeroBased int lastL = Math.min(lines.size(), zeroL + numLinesAround);

        List<String> strings = lines.subList(firstL, lastL);
        return new MessageTextBuilder(strings, firstL, zeroL - firstL);
    }

    /**
     * Sums the line length without the line separation and the characters which matched the line separation pattern
     *
     * @param scanner the scanner from which to read the line's length
     *
     * @return the length of the line with the line separator.
     */
    private int getLineLengthWithLineSeparator(final Scanner scanner) {
        String nline = scanner.nextLine();
        lines.add(nline);
        int lineLength = nline.length();
        final String lineSeparationMatch = scanner.match().group(1);

        if (lineSeparationMatch != null) {
            lineLength += lineSeparationMatch.length();
        }

        return lineLength;
    }

    public int lineNumberFromOffset(int offset) {
        int search = Collections.binarySearch(lineOffsets, offset);
        return search >= 0 ? search + 1 // 1-based line numbers
                           : -(search + 1); // see spec of binarySearch
    }

    public int columnFromOffset(int lineNumber, int offset) {
        int lineIndex = lineNumber - 1;
        if (lineIndex < 0 || lineIndex >= lineOffsets.size()) {
            // no line number found...
            return 0;
        }
        int columnOffset = offset - lineOffsets.get(lineNumber - 1);
        return columnOffset + 1; // 1-based column offsets
    }

    /**
     * Helper object.
     */
    static class MessageTextBuilder {

        private static final String CARET = "^ ";
        /** Line number of the first line of the list in the real document */
        private final @OneBased int first;
        /** Index in the list of the line that has the error. */
        private final @ZeroBased int errorIdx;
        private List<String> lines;

        MessageTextBuilder(List<String> lines, @OneBased int first, int errorIdx) {
            this.lines = lines;
            this.first = first;
            this.errorIdx = errorIdx;
            assert (0 <= errorIdx && errorIdx < lines.size())
                : "Weird indices --- first=" + first + ", errorIdx=" + errorIdx + ", lines=" + lines;
        }

        public String make(boolean supportsAnsiColors, XmlMessageKind kind, XmlException.Severity severity, XmlPosition position, String message) {

            int pad = stringLengthOf(lines.size() + first);

            List<String> withLineNums = IntStream.range(0, lines.size())
                                                 .mapToObj(i -> addLineNum(i, pad))
                                                 .collect(Collectors.collectingAndThen(Collectors.toList(), ArrayList::new));

            String errorLine = addLineNum(errorIdx, pad);
            // diff added by line numbers
            int offset = errorLine.length() - lines.get(errorIdx).length();

            String messageLine = InternalUtil.buildCaretLine(message.trim(),
                                                             position.getColumn() + offset - 1,
                                                             position.getLength());

            String colored = supportsAnsiColors ? severity.withColor(messageLine) : messageLine;

            withLineNums.add(errorIdx + 1, colored);
            withLineNums.add(errorIdx + 2, ""); // skip a line


            return addHeader(kind, severity, position, String.join("\n", withLineNums));
        }

        private int stringLengthOf(int i) {
            return (i + "").length();
        }

        private String addLineNum(@ZeroBased int idx, int pad) {
            return String.format(" %" + pad + "d| %s", 1 + idx + first, lines.get(idx));
        }

        public static String addHeader(XmlMessageKind kind, XmlException.Severity severity, XmlPosition position, String message) {


            String url = position.getSystemId();
            String header = kind.getHeader(severity);
            if (url != null) {
                header += " in " + url;
            }

            return header + "\n" + message;
        }
    }
}
