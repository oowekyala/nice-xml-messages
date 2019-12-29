package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Node;

/**
 * Scanner with known document context.
 */
class FullFilePositioner extends PartialFilePositioner implements XmlPositioner {

    private final NewOffsetScanner scanner;


    /**
     * @param fullFileText Full text of the XML file
     * @param systemId     System ID of the XML file, typically a file name
     */
    public FullFilePositioner(String fullFileText, String systemId) {
        super(fullFileText, systemId);

        this.scanner = new NewOffsetScanner(systemId, textDoc);
    }

    @Override
    public XmlPosition startPositionOf(Node node) {
        return scanner.beginPos(node);
    }
}
