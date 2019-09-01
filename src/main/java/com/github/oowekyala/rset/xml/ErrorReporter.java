package com.github.oowekyala.rset.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 * Reports errors during serialization.
 */
public interface ErrorReporter {


    void warn(Node node, String message);


    void warn(NodeList node, String message);


    XmlParsingException error(Node node, String message);


    XmlParsingException error(Node node, Throwable ex);


    XmlParsingException error(NodeList node, String message);


    XmlParsingException error(SAXParseException throwable);


    class XmlParsingException extends RuntimeException {

        private final Position position;

        XmlParsingException(Position position) {
            this.position = position;
        }
    }
}
