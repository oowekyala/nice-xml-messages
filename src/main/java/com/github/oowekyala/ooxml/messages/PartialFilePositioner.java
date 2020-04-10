package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;
import com.github.oowekyala.ooxml.messages.TextDoc.MessageTextBuilder;

/**
 * @author Clément Fournier
 */
class PartialFilePositioner implements XmlPositioner {

    private static final int NUM_LINES_AROUND = 3;
    protected final TextDoc textDoc;
    private final String systemId;


    /**
     * @param fullFileText Full text of the XML file
     */
    public PartialFilePositioner(String fullFileText, String systemId) {
        this.textDoc = new TextDoc(fullFileText);
        this.systemId = systemId;
    }

    @Override
    public XmlPosition startPositionOf(@Nullable Node node) {
        return XmlPosition.undefinedIn(systemId);
    }

    @Override
    public String makePositionedMessage(XmlPosition position, boolean supportsAnsiColors, XmlMessageKind kind, XmlException.Severity severity, String message) {
        if (position.isUndefined()) {
            return MessageTextBuilder.addHeader(kind, severity, position, message);
        }

        return textDoc.getLinesAround(position.getLine(), NUM_LINES_AROUND)
                      .make(supportsAnsiColors, kind, severity, position, message)
                      .trim();

    }
}
