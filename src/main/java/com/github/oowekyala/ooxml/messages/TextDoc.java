package com.github.oowekyala.ooxml.messages;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    }

    /** Returns the full source. */
    public String getTextString() {
        return sourceCode;
    }

    MessageTextBuilder getLinesAround(@Annots.OneBased int line, int numLinesAround) {
        @Annots.ZeroBased int zeroL = line - 1;
        @Annots.ZeroBased int firstL = Math.max(0, zeroL - numLinesAround + 1);
        @Annots.ZeroBased int lastL = Math.min(lines.size(), zeroL + numLinesAround);

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
        private final @Annots.OneBased int first;
        /** Index in the list of the line that has the error. */
        private final int errorIdx;
        private List<String> lines;

        MessageTextBuilder(List<String> lines, @Annots.OneBased int first, int errorIdx) {
            this.lines = lines;
            this.first = first;
            this.errorIdx = errorIdx;
            assert (0 <= errorIdx && errorIdx < lines.size());
        }


        public String make(boolean supportsAnsiColors, XmlMessageKind kind, Severity severity, XmlPosition position, String message) {

            List<String> withLineNums = IntStream.range(0, lines.size())
                                                 .mapToObj(this::addLineNum)
                                                 .collect(Collectors.collectingAndThen(Collectors.toList(), ArrayList::new));

            String rline = addLineNum(errorIdx);
            int offset = rline.length() - lines.get(errorIdx).length();

            String messageLine = InternalUtil.addNSpacesLeft(CARET, position.getColumn() + offset - 1) + message;

            String colored = supportsAnsiColors ? severity.withColor(messageLine) : messageLine;

            withLineNums.add(errorIdx + 1, colored);
            withLineNums.add(errorIdx + 2, "\n"); // skip a line


            return addHeader(kind, severity, position, String.join("\n", withLineNums));
        }

        private String addLineNum(@Annots.ZeroBased int i) {
            return String.format("%5d| %s", 1 + i + first, lines.get(i));
        }

        public static String addHeader(XmlMessageKind kind, Severity severity, XmlPosition position, String message) {


            String url = position.getSystemId();
            String header = kind.getHeader(severity);
            if (url != null) {
                header += " in " + url;
            }

            return header + "\n" + message;
        }
    }
}
