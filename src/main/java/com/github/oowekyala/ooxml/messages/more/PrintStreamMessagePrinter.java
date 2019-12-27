package com.github.oowekyala.ooxml.messages.more;

import java.io.PrintStream;

/**
 * Implements {@link MessagePrinter} with a pair of {@link PrintStream}s.
 */
public class PrintStreamMessagePrinter implements MessagePrinter {

    private final PrintStream out;
    private final PrintStream err;
    private final boolean supportsColor;
    private final boolean debugEnabled;

    public PrintStreamMessagePrinter(PrintStream out,
                                     PrintStream err,
                                     boolean supportsColor,
                                     boolean debugEnabled) {
        this.out = out;
        this.err = err;
        this.supportsColor = supportsColor;
        this.debugEnabled = debugEnabled;
    }

    public PrintStreamMessagePrinter(boolean supportsColor, boolean debugEnabled) {
        this(System.out, System.err, supportsColor, debugEnabled);
    }

    @Override
    public void error(String msg) {
        err.println(msg);
    }

    @Override
    public void warn(String message) {
        err.println(message);
    }

    @Override
    public void info(String message) {
        out.println(message);
    }

    @Override
    public void debug(String message) {
        if (debugEnabled) {
            err.println(message);
        }
    }

    @Override
    public boolean supportsAnsiColors() {
        return supportsColor;
    }
}
