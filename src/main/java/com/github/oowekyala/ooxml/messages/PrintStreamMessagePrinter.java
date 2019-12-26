package com.github.oowekyala.ooxml.messages;

import java.io.PrintStream;

/**
 * Implements {@link MessagePrinter} with a pair of {@link PrintStream}s.
 */
public class PrintStreamMessagePrinter implements MessagePrinter {

    private final PrintStream out;
    private final PrintStream err;
    private final boolean supportsColor;

    public PrintStreamMessagePrinter(PrintStream out,
                                     PrintStream err,
                                     boolean supportsColor) {
        this.out = out;
        this.err = err;
        this.supportsColor = supportsColor;
    }

    @Override
    public void error(String msg) {
        err.println("\n[error] " + msg);
    }

    @Override
    public void warn(String message) {
        err.println("\n[warning] " + message);
    }

    @Override
    public void info(String message) {
        out.println("\n[info] " + message);
    }

    @Override
    public String applyAnsi(AnsiCode color, String string) {
        return supportsColor ? color.apply(string) : string;
    }
}
