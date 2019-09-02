package com.github.oowekyala.rset.xml;

import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import com.github.oowekyala.rset.xml.ErrorReporter.Message.Kind;
import com.github.oowekyala.rset.xml.ErrorReporter.Message.Templated;

public class DefaultErrorReporter implements ErrorReporter {

    private TextDoc textDoc;
    private final MessagePrinter printer;

    public DefaultErrorReporter(MessagePrinter printer) {
        this.textDoc = null;
        this.printer = printer;
    }


    @Override
    public void warn(Node node, String message, Object... args) {
        String toPrint = makeMessage(LineNumberScanner.beginPos(node), new Templated(Kind.VALIDATION_WARNING, message, args));
        printer.warn(toPrint);
    }

    protected XmlParseException pp(XmlParseException ex) {
        printer.error(makeMessage(ex.getPosition(), ex.getMessageObj()));
        return ex;
    }

    private String makeMessage(Position position, Message message) {
        if (!position.equals(Position.UNDEFINED) && textDoc != null) {
            return textDoc.getLinesAround(position.getLine())
                          .make(position, message);
        } else {
            return message.toString();
        }
    }


    @Override
    public XmlParseException error(Node node, String template, Object... args) {
        Position pos = LineNumberScanner.beginPos(node);
        Templated message = new Templated(Kind.VALIDATION_ERROR, template, args);
        return pp(new XmlParseException(pos, new Message.Wrapper(message, makeMessage(pos, message))));
    }

    @Override
    public XmlParseException error(Node node, Throwable ex) {
        Position pos = LineNumberScanner.beginPos(node);
        Message message = messageFromException(ex, Kind.VALIDATION_ERROR);
        return pp(new XmlParseException(pos, new Message.Wrapper(message, makeMessage(pos, message)), ex));
    }


    @Override
    public XmlParseException error(SAXParseException throwable) {
        return pp(convertSax(Kind.VALIDATION_ERROR, throwable));
    }

    @Override
    public XmlParseException warn(SAXParseException throwable) {
        return pp(convertSax(Kind.VALIDATION_WARNING, throwable));
    }

    @Override
    public XmlParseException fatal(SAXParseException throwable) {
        throw convertSax(Kind.PARSING_ERROR, throwable);
    }

    private XmlParseException convertSax(Kind kind, SAXParseException throwable) {
        Position pos = new Position(throwable.getLineNumber() - 1, throwable.getColumnNumber());
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
