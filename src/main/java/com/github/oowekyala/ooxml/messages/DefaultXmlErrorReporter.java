package com.github.oowekyala.ooxml.messages;

import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.USER_VALIDATION;

import java.util.Formatter;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;

/**
 * Simple implementation of an error reporter. Only needs a
 * {@link XmlMessageHandler} and an {@link XmlPositioner}.
 */
public class DefaultXmlErrorReporter implements XmlErrorReporter {

    private final XmlPositioner positioner;
    private final XmlMessageHandler printer;

    public DefaultXmlErrorReporter(XmlMessageHandler printer, XmlPositioner positioner) {
        this.positioner = positioner;
        this.printer = printer;
    }

    @Override
    public void warn(Node node, String message, Object... args) {
        assert message != null : "Message is null!";
        XmlException ex = createEntry(node, Severity.WARNING, printer.supportsAnsiColors(), template(message, args), null);
        printer.accept(ex);
    }


    @Override
    public XmlException error(Node node, String message, Object... args) {
        assert message != null : "Message is null!";
        XmlException ex = createEntry(node, Severity.ERROR, printer.supportsAnsiColors(), template(message, args), null);
        printer.accept(ex);
        return ex;
    }

    @Override
    public XmlException error(Node node, Throwable cause) {
        assert cause != null : "Cause is null!";
        XmlException ex = createEntry(node, Severity.ERROR, printer.supportsAnsiColors(), cause.getMessage(), cause);
        printer.accept(ex);
        return ex;
    }

    @Override
    public XmlException fatal(Node node, String message, Object... args) {
        assert message != null : "Message is null!";
        XmlException ex = createEntry(node, Severity.FATAL, printer.supportsAnsiColors(), template(message, args), null);
        printer.accept(ex);
        throw ex;
    }

    @Override
    public XmlException fatal(Node node, Throwable cause) {
        assert cause != null : "Cause is null!";
        XmlException ex = createEntry(node, Severity.FATAL, printer.supportsAnsiColors(), cause.getMessage(), cause);
        printer.accept(ex);
        throw ex;
    }

    /**
     * Template the message with the given args. By default uses {@link String#format(String, Object...)
     * String::format}. Override to use eg {@link Formatter}. If no arguments
     * are present, templating is applied regardless.
     *
     * @param message Template
     * @param args    Template arguments
     */
    protected String template(String message, Object... args) {
        return String.format(message, args);
    }


    private XmlException createEntry(@Nullable Node node,
                                     Severity level,
                                     boolean useColors,
                                     String message,
                                     Throwable cause) {

        XmlPosition pos = positioner.startPositionOf(node);
        String fullMessage = positioner.makePositionedMessage(pos, useColors, USER_VALIDATION, level, message);

        return new XmlException(pos, fullMessage, message, USER_VALIDATION, level, cause);
    }


    @Override
    public void close() {

    }

}
