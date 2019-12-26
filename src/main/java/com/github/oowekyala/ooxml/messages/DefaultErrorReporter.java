package com.github.oowekyala.ooxml.messages;

import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.PARSING_ERROR;
import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.PARSING_WARNING;
import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.VALIDATION_ERROR;
import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.VALIDATION_WARNING;

import org.w3c.dom.Node;

public class DefaultErrorReporter implements ErrorReporter {

    private final XmlPositioner positioner;
    private final MessagePrinter printer;

    public DefaultErrorReporter(MessagePrinter printer,
                                XmlPositioner positioner) {
        this.positioner = positioner;
        this.printer = printer;
    }

    @Override
    public void warn(Node node, String message, Object... args) {
        XmlPosition pos = positioner.startPositionOf(node);
        String formatted = String.format(message, args);
        String toPrint = makeMessage(pos, VALIDATION_WARNING, formatted);
        printer.warn(toPrint);
    }

    private XmlParseException printAndPass(XmlParseException ex) {
        printer.error(ex.toString());
        return ex;
    }

    private String makeMessage(XmlPosition position, XmlMessageKind kind, String message) {
        return positioner.makePositionedMessage(position, printer.supportsAnsiColors(), kind, message);
    }


    @Override
    public XmlParseException error(Node node, String template, Object... args) {
        XmlPosition pos = positioner.startPositionOf(node);
        String simpleMessage = String.format(template, args);
        return printAndPass(createException(pos, simpleMessage, null));
    }


    @Override
    public XmlParseException error(Node node, Throwable ex) {
        XmlPosition pos = positioner.startPositionOf(node);
        return printAndPass(createException(pos, ex.getMessage(), ex));
    }


    private XmlParseException createException(XmlPosition pos, String formatted, Throwable ex) {
        String fullMessage = makeMessage(pos, VALIDATION_ERROR, formatted);
        return new XmlParseException(pos, fullMessage, formatted, VALIDATION_ERROR, ex);
    }


    @Override
    public XmlParseException parseError(boolean warn, Throwable throwable) {
        final XmlMessageKind exKind = warn ? PARSING_WARNING : PARSING_ERROR;

        XmlPosition pos = XmlPositioner.extractPosition(throwable);


        final XmlParseException xpe;
        if (pos.equals(XmlPosition.UNDEFINED)) {
            // unknown exception
            xpe = new XmlParseException(XmlPosition.UNDEFINED,
                                        "Parse error\n\t" + throwable.getMessage(),
                                        throwable.getMessage(),
                                        exKind,
                                        throwable);

        } else {
            String message = makeMessage(pos, exKind, throwable.getMessage());
            xpe = new XmlParseException(pos, message, throwable.getMessage(), exKind, throwable);
        }

        if (warn) {
            printer.error(xpe.toString());
            return xpe;
        } else {
            throw xpe;
        }
    }

    @Override
    public void close() {

    }

}
