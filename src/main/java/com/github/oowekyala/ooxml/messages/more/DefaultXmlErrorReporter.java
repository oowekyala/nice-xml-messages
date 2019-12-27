package com.github.oowekyala.ooxml.messages.more;

import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.VALIDATION_ERROR;
import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.VALIDATION_WARNING;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.XmlException;
import com.github.oowekyala.ooxml.messages.XmlPositioner;

/**
 * This error reporter uses the default
 */
public class DefaultXmlErrorReporter implements XmlErrorReporter {

    private final XmlPositioner positioner;
    private final MessagePrinter printer;

    public DefaultXmlErrorReporter(MessagePrinter printer, XmlPositioner positioner) {
        this.positioner = positioner;
        this.printer = printer;
    }

    @Override
    public void warn(Node node, String message, Object... args) {
        XmlException ex = positioner.createEntry(node, VALIDATION_WARNING, printer.supportsAnsiColors(), message, args);
        printer.warn(ex.toString());
    }


    @Override
    public XmlException error(Node node, String template, Object... args) {
        XmlException ex = positioner.createEntry(node, VALIDATION_ERROR, printer.supportsAnsiColors(), template, args);
        printer.error(ex.toString());
        return ex;
    }


    @Override
    public XmlException error(Node node, Throwable cause) {
        XmlException ex = positioner.createEntry(node, VALIDATION_ERROR, printer.supportsAnsiColors(), cause);
        printer.error(ex.toString());
        return ex;
    }

    @Override
    public void close() {

    }

}
