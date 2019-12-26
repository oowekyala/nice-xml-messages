package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Document;

/**
 * A simple pair of a parsed {@link Document} and an {@link ErrorReporter}
 * that can position its nodes.
 */
public class PositionedXmlDoc {

    private final Document document;
    private final ErrorReporter reporter;

    PositionedXmlDoc(Document document, ErrorReporter reporter) {
        this.document = document;
        this.reporter = reporter;
    }

    public Document getDocument() {
        return document;
    }

    public ErrorReporter getReporter() {
        return reporter;
    }
}
