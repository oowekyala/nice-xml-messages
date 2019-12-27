package com.github.oowekyala.ooxml.messages.more;

import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.USER_VALIDATION;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Severity;
import com.github.oowekyala.ooxml.messages.XmlException;
import com.github.oowekyala.ooxml.messages.XmlMessageHandler;
import com.github.oowekyala.ooxml.messages.XmlPosition;
import com.github.oowekyala.ooxml.messages.XmlPositioner;

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
        XmlException ex = createEntry(node, Severity.WARNING, printer.supportsAnsiColors(), template(message, args), null);
        printer.accept(ex);
    }


    @Override
    public XmlException error(Node node, String message, Object... args) {
        XmlException ex = createEntry(node, Severity.ERROR, printer.supportsAnsiColors(), template(message, args), null);
        printer.accept(ex);
        return ex;
    }

    @Override
    public XmlException error(Node node, Throwable cause) {
        XmlException ex = createEntry(node, Severity.ERROR, printer.supportsAnsiColors(), cause.getMessage(), cause);
        printer.accept(ex);
        return ex;
    }

    @Override
    public XmlException fatal(Node node, String message, Object... args) {
        XmlException ex = createEntry(node, Severity.FATAL, printer.supportsAnsiColors(), template(message, args), null);
        printer.accept(ex);
        throw ex;
    }

    @Override
    public XmlException fatal(Node node, Throwable cause) {
        XmlException ex = createEntry(node, Severity.FATAL, printer.supportsAnsiColors(), cause.getMessage(), cause);
        printer.accept(ex);
        throw ex;
    }

    private String template(String message, Object... args) {
        return args == null || args.length == 0
               ? message
               : String.format(message, args);
    }


    private XmlException createEntry(Node node,
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
