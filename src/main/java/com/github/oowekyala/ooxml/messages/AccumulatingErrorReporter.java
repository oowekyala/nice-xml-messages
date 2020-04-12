package com.github.oowekyala.ooxml.messages;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.oowekyala.ooxml.messages.XmlException.Severity;

/**
 * Accumulates messages and does not display them until the
 * reporter is closed.
 */
public class AccumulatingErrorReporter extends DefaultXmlErrorReporter {

    private final EnumMap<Severity, Map<String, List<XmlException>>> entries = new EnumMap<>(Severity.class);
    private final Severity minSeverity;


    public AccumulatingErrorReporter(XmlMessageHandler printer,
                                     XmlPositioner positioner,
                                     Severity minSeverity) {
        super(printer, positioner);
        this.minSeverity = minSeverity;
    }


    @Override
    protected void handle(XmlException ex, String message) {
        entries.computeIfAbsent(ex.getSeverity(), s -> new HashMap<>())
               .computeIfAbsent(message, m -> new ArrayList<>())
               .add(ex);
    }


    @Override
    public void close() {
        close(minSeverity, minSeverity);
    }


    /**
     * Close the reporter and print the accumulated exceptions.
     * The two parameters select how entries are printed to
     * the {@link #printer}, they are dispatched to the methods
     * {@link #dontPrint(Severity, Map)}, {@link #printFully(Severity, String, List)}
     * and {@link #printSummary(Severity, String, List)}.
     */
    public void close(Severity minSeverityToPrintSummary, Severity minSeverityToPrintFully) {
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
    protected void printFully(Severity severity, String message, List<XmlException> entry) {
        for (XmlException e : entry) {
            printer.accept(e);
        }
    }


    /**
     * Print a summary line, not every one of the entries.
     *
     * @param severity severity of the message
     * @param message  Message with which the entry was reported
     * @param entry    A nonempty list
     */
    protected void printSummary(Severity severity, String message, List<XmlException> entry) {
        if (entry.size() > 1) {
            XmlException first = entry.get(0);
            XmlMessageKind kind = first.getKind();
            printer.printMessageLn(kind,
                                   severity,
                                   "There were " + entry.size() + " " + severity.toString().toLowerCase(Locale.ROOT)
                                       + " like the following one:");
            printer.accept(first);
        } else {
            printer.accept(entry.get(0));
        }
    }


    /**
     * Handle ignored messages.
     *
     * @param severity         Severity
     * @param entriesByMessage Entries for the given severity,
     *                         indexed by their message
     */
    protected void dontPrint(Severity severity, Map<String, List<XmlException>> entriesByMessage) {
        // do nothing by default
    }


}
