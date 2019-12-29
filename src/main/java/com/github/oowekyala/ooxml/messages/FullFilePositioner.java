package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Scanner with known document context.
 */
class FullFilePositioner extends PartialFilePositioner implements XmlPositioner {

    private final OffsetScanner scanner;


    /**
     * @param fullFileText Full text of the XML file
     * @param systemId     System ID of the XML file, typically a file name
     * @param document     Document node of the XML document
     */
    public FullFilePositioner(String fullFileText, String systemId, Document document) {
        super(fullFileText, systemId);

        this.scanner = new OffsetScanner(systemId, textDoc);

        scanner.determineLocation(document.getDocumentElement(), 0);
    }

    @Override
    public XmlPosition startPositionOf(Node node) {
        return scanner.beginPos(node);
    }
}
