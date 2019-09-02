package com.github.oowekyala.oxml;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import com.github.oowekyala.oxml.ErrorReporter.Message.Kind;
import com.github.oowekyala.oxml.ErrorReporter.Message.Templated;

public class DefaultErrorReporter implements ErrorReporter {

    private TextDoc textDoc;
    private final MessagePrinter printer;

    public DefaultErrorReporter(MessagePrinter printer) {
        this.textDoc = null;
        this.printer = printer;
    }


    @Override
    public void warn(Node node, String message, Object... args) {
        String toPrint = makeMessage(getBeginPos(node), new Templated(Kind.VALIDATION_WARNING, message, args));
        printer.warn(toPrint);
    }

    protected XmlParseException pp(XmlParseException ex) {
        return pp(ex, true);
    }

    protected XmlParseException pp(XmlParseException ex, boolean condition) {
        if (condition) {
            printer.error(ex.toString());
        }
        return ex;
    }

    private String makeMessage(Position position, Message message) {
        if (!position.equals(Position.UNDEFINED) && textDoc != null) {
            return textDoc.getLinesAround(position.getLine())
                          .make(printer.supportsAnsiColor(), message.getKind(), position, message);
        } else {
            return message.toString();
        }
    }


    @Override
    public XmlParseException error(Node node, String template, Object... args) {
        Position pos = getBeginPos(node);
        Templated message = new Templated(Kind.VALIDATION_ERROR, template, args);
        return pp(new XmlParseException(pos, new Message.Wrapper(message, makeMessage(pos, message))));
    }

    @Override
    public XmlParseException error(Node node, Throwable ex) {
        Position pos = getBeginPos(node);
        Message message = messageFromException(ex, Kind.VALIDATION_ERROR);
        return pp(new XmlParseException(pos, new Message.Wrapper(message, makeMessage(pos, message)), ex));
    }

    protected final Position getBeginPos(Node node) {
        return OffsetScanner.beginPos(node);
    }


    @Override
    public XmlParseException error(boolean warn, SAXParseException throwable) {
        XmlParseException xpe = convertException(getExKind(warn), throwable, throwable.getLineNumber(), throwable.getColumnNumber(), throwable.getSystemId());
        return pp(xpe, warn);
    }

    @Override
    public XmlParseException error(boolean warn, TransformerException throwable) {
        if (throwable.getCause() instanceof SAXParseException) {
            return error(warn, ((SAXParseException) throwable.getCause()));
        }
        SourceLocator locator = throwable.getLocator();
        int line = locator == null ? -1 : locator.getLineNumber();
        int column = locator == null ? -1 : locator.getColumnNumber();
        String systemId = locator == null ? null : locator.getSystemId();
        XmlParseException xpe = convertException(getExKind(warn), throwable, line, column, systemId);
        return pp(xpe, warn);
    }

    private Kind getExKind(boolean warn) {
        return warn ? Kind.PARSING_WARNING : Kind.PARSING_ERROR;
    }

    private XmlParseException convertException(Kind kind, Throwable throwable, int line, int column, String systemId) {
        Position pos = new Position(systemId, line, column);
        Message message = messageFromException(throwable, kind);
        return new XmlParseException(pos, new Message.Wrapper(message, makeMessage(pos, message)), throwable);
    }

    private Message messageFromException(Throwable throwable, Kind kind) {
        return new Templated(kind, throwable.getMessage());
    }

    @Override
    public void close() {


    }

    @Override
    public void setDocument(String read) {
        this.textDoc = new TextDoc(read);
    }
}
