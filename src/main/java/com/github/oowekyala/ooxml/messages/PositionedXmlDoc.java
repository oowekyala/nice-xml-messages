package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Document;

/**
 * A simple pair of a parsed {@link Document} and an {@link XmlPositioner}
 * that can position its nodes.
 */
public final class PositionedXmlDoc {

    private final Document document;
    private final XmlPositioner reporter;

    PositionedXmlDoc(Document document, XmlPositioner reporter) {
        this.document = document;
        this.reporter = reporter;
    }

    public Document getDocument() {
        return document;
    }

    public XmlPositioner getPositioner() {
        return reporter;
    }
}
