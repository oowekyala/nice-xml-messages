package com.github.oowekyala.rset.xml;

import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

/**
 * Reports errors during serialization.
 */
public interface ErrorReporter {


    void warn(Node node, String message, Object... args);


    XmlParsingException error(Node node, String message, Object... args);


    XmlParsingException error(Node node, Throwable ex);


    XmlParsingException error(SAXParseException throwable);


    void close();


    abstract class Message {

        public abstract String toString();

        public static class Templated extends Message {

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

        XmlParsingException(Position position, Message message) {
            super(message.toString());
            this.position = position;
            this.message = message;
        }

        XmlParsingException(Position position, Message message, Throwable cause) {
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
