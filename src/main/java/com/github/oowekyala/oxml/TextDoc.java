package com.github.oowekyala.oxml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.github.oowekyala.oxml.Util.MessageTextBuilder;

class TextDoc {

    /**
     * Number of lines above and below the error line to display.
     */
    private static final int SURROUND_SIZE = 3;

    /**
     * This list has one entry for each line, denoting the start offset of the line.
     * The start offset of the next line includes the length of the line terminator
     * (1 for \r|\n, 2 for \r\n).
     */
    private final List<Integer> lineOffsets = new ArrayList<>();
    private final List<String> lines = new ArrayList<>();
    private final int sourceCodeLength;
    private final String sourceCode;

    public TextDoc(String sourceCode) {
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
    public String getSourceCode() {
        return sourceCode;
    }

    MessageTextBuilder getLinesAround(int line) {
        int first = Math.max(0, line - SURROUND_SIZE);
        int last = Math.min(lines.size(), line + SURROUND_SIZE);

        List<String> strings = lines.subList(first, last);
        return new MessageTextBuilder(strings, first, line - first);
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

}