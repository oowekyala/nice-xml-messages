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


    abstract class Message {

        public abstract String toString();

        public class Templated extends Message {

            private final String template;
            private final Object[] args;

            public Templated(String template, Object... args) {
                this.template = template;
                this.args = args;
            }

            @Override
            public String toString() {
                return String.format(template, args);
            }
        }
    }

    class XmlParsingException extends RuntimeException {

        private final Position position;
        private final Message message;

        private XmlParsingException(Position position, Message message) {
            super(message.toString());
            this.position = position;
            this.message = message;
        }

        private XmlParsingException(Position position, Message message, Throwable cause) {
            super(message.toString(), cause);
            this.position = position;
            this.message = message;
        }

        public Position getPosition() {
            return position;
        }


        public Message getMessageObj() {
            return message;
        }
    }
}
