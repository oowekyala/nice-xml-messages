package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Document;

/**
 * A simple pair of a parsed {@link Document} and an {@link XmlErrorReporter}
 * that can position its nodes.
 */
public class PositionedXmlDoc {

    private final Document document;
    private final XmlErrorReporter reporter;

    PositionedXmlDoc(Document document, XmlErrorReporter reporter) {
        this.document = document;
        this.reporter = reporter;
    }

    public Document getDocument() {
        return document;
    }

    public XmlErrorReporter getReporter() {
        return reporter;
    }
}
