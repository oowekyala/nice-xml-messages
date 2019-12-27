package com.github.oowekyala.ooxml.messages.more;

import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.USER_VALIDATION;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Severity;
import com.github.oowekyala.ooxml.messages.XmlException;
import com.github.oowekyala.ooxml.messages.XmlPosition;
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
        XmlException ex = createEntry(node, Severity.WARNING, printer.supportsAnsiColors(), template(message, args), null);
        printer.warn(ex.toString());
    }


    @Override
    public XmlException error(Node node, String message, Object... args) {
        XmlException ex = createEntry(node, Severity.ERROR, printer.supportsAnsiColors(), template(message, args), null);
        printer.error(ex.toString());
        return ex;
    }

    @Override
    public XmlException error(Node node, Throwable cause) {
        XmlException ex = createEntry(node, Severity.ERROR, printer.supportsAnsiColors(), cause.getMessage(), cause);
        printer.error(ex.toString());
        return ex;
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
