package com.github.oowekyala.rset.xml;

import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

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
        String toPrint = makeMessage(LineNumberScanner.beginPos(node), new Templated(message, args));
        printer.warn(toPrint);
    }

    protected XmlParsingException pp(XmlParsingException ex) {
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
    public XmlParsingException error(Node node, String message, Object... args) {
        return pp(new XmlParsingException(LineNumberScanner.beginPos(node), new Templated(message, args)));
    }

    @Override
    public XmlParsingException error(Node node, Throwable ex) {
        return pp(new XmlParsingException(LineNumberScanner.beginPos(node), messageFromException(ex), ex));
    }


    @Override
    public XmlParsingException error(SAXParseException throwable) {
        Position pos = new Position(throwable.getLineNumber(), throwable.getColumnNumber());
        return pp(new XmlParsingException(pos, messageFromException(throwable), throwable));
    }

    private Message messageFromException(Throwable throwable) {
        return new Templated(throwable.getMessage());
    }

    @Override
    public void close() {


    }

    @Override
    public void setDocument(String read) {
        this.textDoc = new TextDoc(read);
    }
}
