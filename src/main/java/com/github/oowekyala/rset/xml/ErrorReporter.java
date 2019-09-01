package com.github.oowekyala.rset.xml;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reports errors during serialization.
 */
public interface ErrorReporter {


    void warn(Node node, String message);


    void warn(NodeList node, String message);


    XmlParsingException error(Node node, String message);


    XmlParsingException error(Node node, Throwable ex);


    XmlParsingException error(NodeList node, String message);


    class XmlParsingException extends RuntimeException {

        private final List<XmlParsingException> aggregate;

        public XmlParsingException(List<XmlParsingException> aggregate) {
            this.aggregate = aggregate;
        }

        // todo position


    }
}
