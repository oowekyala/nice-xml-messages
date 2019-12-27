package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Node;

/**
 * @author Clément Fournier
 */
class PartialFilePositioner implements XmlPositioner {

    private static final int NUM_LINES_AROUND = 3;
    protected final TextDoc textDoc;


    /**
     * @param fullFileText Full text of the XML file
     */
    public PartialFilePositioner(String fullFileText) {
        this.textDoc = new TextDoc(fullFileText);
    }

    @Override
    public XmlPosition startPositionOf(Node node) {
        return XmlPosition.UNDEFINED;
    }

    @Override
    public String makePositionedMessage(XmlPosition position, boolean supportsAnsiColors, XmlMessageKind kind, String message) {
        if (position.equals(XmlPosition.UNDEFINED)) {
            return message;
        }
        return textDoc.getLinesAround(position.getLine(), NUM_LINES_AROUND)
                      .make(supportsAnsiColors, kind, position, message)
                      .trim();

    }
}
