package com.github.oowekyala.ooxml.messages.more;

import java.io.PrintStream;

import com.github.oowekyala.ooxml.messages.XmlException;
import com.github.oowekyala.ooxml.messages.XmlMessageHandler;

/**
 * Implements {@link XmlMessageHandler} with a pair of {@link PrintStream}s.
 */
public class PrintStreamMessageHandler implements XmlMessageHandler {

    private final PrintStream out;
    private final PrintStream err;
    private final boolean supportsColor;
    private final boolean debugEnabled;

    public PrintStreamMessageHandler(PrintStream out,
                                     PrintStream err,
                                     boolean supportsColor,
                                     boolean debugEnabled) {
        this.out = out;
        this.err = err;
        this.supportsColor = supportsColor;
        this.debugEnabled = debugEnabled;
    }

    public PrintStreamMessageHandler(boolean supportsColor, boolean debugEnabled) {
        this(System.out, System.err, supportsColor, debugEnabled);
    }

    @Override
    public boolean supportsAnsiColors() {
        return supportsColor;
    }

    @Override
    public void accept(XmlException entry) {
        switch (entry.getSeverity()) {
        case INFO:
            out.println(entry.toString());
            break;
        case DEBUG:
            if (!debugEnabled) {
                break;
            }
        case WARNING:
        case ERROR:
            err.println(entry.toString());
            break;
        case FATAL:
            err.println(entry.toString());
            throw entry;
        }
    }
}
