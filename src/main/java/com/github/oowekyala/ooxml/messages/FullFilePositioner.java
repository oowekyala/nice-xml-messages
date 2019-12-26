package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Scanner with known document context.
 */
class FullFilePositioner extends PartialFilePositioner implements XmlPositioner {

    private final OffsetScanner scanner;


    /**
     * @param systemId     System ID of the XML file, typically a file name
     * @param fullFileText Full text of the XML file
     * @param document     Document node of the XML document
     */
    public FullFilePositioner(String systemId, String fullFileText, Document document) {
        super(fullFileText);

        this.scanner = new OffsetScanner(systemId);

        scanner.determineLocation(document.getDocumentElement(), textDoc, 0);
    }

    @Override
    public XmlPosition startPositionOf(Node node) {
        return scanner.beginPos(node);
    }
}
