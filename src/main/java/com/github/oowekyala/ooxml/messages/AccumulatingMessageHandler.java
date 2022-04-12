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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Accumulates messages and does not display them until the
 * reporter is closed.
 */
public abstract class AccumulatingMessageHandler implements XmlMessageHandler, AutoCloseable {

    private final EnumMap<XmlSeverity, Map<String, List<XmlException>>> entries = new EnumMap<>(XmlSeverity.class);
    protected final XmlSeverity minSeverity;
    protected final XmlMessageHandler basePrinter;


    public AccumulatingMessageHandler(XmlMessageHandler basePrinter, XmlSeverity minSeverity) {
        this.basePrinter = basePrinter;
        this.minSeverity = minSeverity;
    }


    @Override
    public void accept(XmlException ex) {
        entries.computeIfAbsent(ex.getSeverity(), s -> new HashMap<>())
               .computeIfAbsent(ex.getSimpleMessage(), m -> new ArrayList<>())
               .add(ex);
    }

    public void close() {
        close(minSeverity, minSeverity);
    }


    /**
     * Close the reporter and print the accumulated exceptions.
     * The two parameters select how entries are printed to
     * the {@link #basePrinter}, they are dispatched to the methods
     * {@link #dontPrint(XmlSeverity, Map)}, {@link #printFully(XmlSeverity, String, List)}
     * and {@link #printSummary(XmlSeverity, String, List)}.
     */
    public void close(XmlSeverity minSeverityToPrintSummary, XmlSeverity minSeverityToPrintFully) {
        entries.forEach(((severity, entriesByMessage) -> {
            if (severity.compareTo(minSeverityToPrintFully) >= 0) {
                entriesByMessage.forEach((message, entry) -> printFully(severity, message, entry));
            } else if (severity.compareTo(minSeverityToPrintSummary) >= 0) {
                entriesByMessage.forEach((message, entry) -> printSummary(severity, message, entry));
            } else {
                dontPrint(severity, entriesByMessage);
            }
        }));
    }


    /**
     * Print "fully", the default prints all entries. This is
     * so that previous errors are not forgotten.
     *
     * @param severity severity of the message
     * @param message  Message with which the entry was reported
     * @param entry    A nonempty list
     */
    protected void printFully(XmlSeverity severity, String message, List<XmlException> entry) {
        for (XmlException e : entry) {
            basePrinter.accept(e);
        }
    }


    /**
     * Print a summary line, not every one of the entries.
     *
     * @param severity severity of the message
     * @param message  Message with which the entry was reported
     * @param entry    A nonempty list
     */
    protected void printSummary(XmlSeverity severity, String message, List<XmlException> entry) {
        XmlException first = entry.get(0);
        if (entry.size() > 1) {
            XmlMessageKind kind = first.getKind();
            basePrinter.printMessageLn(kind,
                                              severity,
                                              "There were " + entry.size() + " "
                                                  + severity.toString().toLowerCase(Locale.ROOT)
                                                  + " like the following one:");
        }
        basePrinter.accept(first);
    }


    /**
     * Handle ignored messages.
     *
     * @param severity         Severity
     * @param entriesByMessage Entries for the given severity,
     *                         indexed by their message
     */
    protected void dontPrint(XmlSeverity severity, Map<String, List<XmlException>> entriesByMessage) {
        // do nothing by default
    }


}
