package com.github.oowekyala.rset.xml;

import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import com.github.oowekyala.rset.xml.ErrorReporter.Message.Kind;

/**
 * Reports errors during serialization.
 */
public interface ErrorReporter {


    void warn(Node node, String message, Object... args);


    XmlParseException error(Node node, String message, Object... args);


    XmlParseException error(Node node, Throwable ex);


    XmlParseException error(SAXParseException throwable);


    XmlParseException fatal(SAXParseException throwable);


    XmlParseException warn(SAXParseException throwable);


    void close();

    void setDocument(String read);


    abstract class Message {

        private final Kind kind;

        protected Message(Kind kind) {
            this.kind = kind;
        }

        Kind getKind() {
            return kind;
        }

        enum Kind {
            VALIDATION_ERROR("XML validation error"),
            VALIDATION_WARNING("XML validation warning"),
            PARSING_ERROR("XML parsing error");

            public static final String IN_FILE = "in %s";

            private final String template;

            Kind(String s) {
                template = s;
            }

            public String getTemplate() {
                return template;
            }
        }

        public abstract String toString();

        public static class Templated extends Message {

            private final String template;
            private final Object[] args;

            public Templated(Kind kind, String template, Object... args) {
                super(kind);
                this.template = template;
                this.args = args;
            }

            @Override
            public String toString() {
                return String.format(template, args);
            }
        }

        static class Wrapper extends Message {

            private final Message base;
            private final String eval;

            public Wrapper(Message base, String eval) {
                super(base.getKind());
                this.base = base;
                this.eval = eval;
            }

            @Override
            public String toString() {
                return eval;
            }
        }
    }

    class XmlParseException extends RuntimeException {

        private final Position position;
        private final Message message;

        XmlParseException(Position position, Message message) {
            super(message.toString());
            this.position = position;
            this.message = message;
        }

        XmlParseException(Position position, Message message, Throwable cause) {
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

        @Override
        public String toString() {
            String header = message.getKind().getTemplate();
            String url = getPosition().getFileUrlOrWhatever();
            if (url != null) {
                header = header + Kind.IN_FILE;
            }
            return String.format(header + "%n", url) + message;
        }
    }
}
