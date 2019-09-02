package com.github.oowekyala.oxml;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import com.github.oowekyala.oxml.Util.AnsiColor;

/**
 * Reports errors during serialization.
 */
public interface ErrorReporter {


    void warn(Node node, String message, Object... args);


    XmlParseException error(Node node, String message, Object... args);


    XmlParseException error(Node node, Throwable ex);


    XmlParseException error(boolean warn, SAXParseException throwable);

    XmlParseException error(boolean warn, TransformerException throwable);


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
            VALIDATION_WARNING("XML validation warning", AnsiColor.COL_YELLOW),
            VALIDATION_ERROR("XML validation error", AnsiColor.COL_RED),
            PARSING_WARNING("XML validation warning", AnsiColor.COL_YELLOW),
            PARSING_ERROR("XML parsing error", AnsiColor.COL_RED);

            private final String template;
            private final AnsiColor color;

            Kind(String s, AnsiColor color) {
                template = s;
                this.color = color;
            }

            public String addColor(String str) {
                return color.apply(str);
            }

            public String getHeader(/*Nullable*/String fileLoc) {
                return fileLoc == null ? template : template + " in " + fileLoc;
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
            String url = getPosition().getFileUrlOrWhatever();
            return message.getKind().getHeader(url) + System.lineSeparator() + message;
        }
    }
}
