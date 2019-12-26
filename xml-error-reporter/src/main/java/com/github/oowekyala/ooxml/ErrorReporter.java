package com.github.oowekyala.ooxml;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.MessagePrinter.AnsiCode;

/**
 * Reports errors in an XML document. Implementations have a way to
 * associate nodes with their location in the document.
 */
public interface ErrorReporter {

    /**
     * Print a warning at the location of a node.
     *
     * @param node    XML node which owns the warning
     * @param message Message, possibly templated
     * @param args    Formatter arguments
     */
    void warn(Node node, String message, Object... args);


    /**
     * Report an error at the location of a node. Whether this throws
     * the exception is an implementation detail.
     *
     * @param node    XML node which owns the warning
     * @param message Message, possibly templated
     * @param args    Formatter arguments
     */
    XmlParseException error(Node node, String message, Object... args);


    /**
     * Report an external error at the location of a node. Whether this
     * throws the exception is an implementation detail.
     *
     * @param node XML node which owns the warning
     * @param ex   Cause exception
     */
    XmlParseException error(Node node, Throwable ex);


    /**
     * An error occurring in the parsing phase.
     *
     * @param warn      Whether this is a warning
     * @param throwable Cause
     *
     * @return An exception. If "warn" is true, the exception
     *     is logged, otherwise it's thrown, and not logged directly.
     *
     * @throws XmlParseException If "warn" is false. In that case, to improve compiler
     *                           control-flow analysis, you can call this method with
     *                           a throw statement as well (eg {@code throw reporter.parseError(false, ex);},
     *                           even though this method throws).
     */
    XmlParseException parseError(boolean warn, Throwable throwable);


    /**
     * End the reporting phase. For example, if this reporter is configured
     * to accumulate errors, this should throw the accumulated errors. This
     * could also print some footer or anything.
     */
    void close();


    interface ErrorReporterFactory {

        ErrorReporter create(String xmlDocument);

    }


    abstract class Message {

        private final Kind kind;

        protected Message(Kind kind) {
            this.kind = kind;
        }

        Kind getKind() {
            return kind;
        }

        enum Kind {
            VALIDATION_WARNING("XML validation warning", AnsiCode.COL_YELLOW),
            VALIDATION_ERROR("XML validation error", AnsiCode.COL_RED),
            PARSING_WARNING("XML validation warning", AnsiCode.COL_YELLOW),
            PARSING_ERROR("XML parsing error", AnsiCode.COL_RED);

            private final String template;
            private final AnsiCode color;

            Kind(String s, AnsiCode color) {
                template = s;
                this.color = color;
            }

            public String addColor(String str) {
                return color.apply(str);
            }

            public AnsiCode getColor() {
                return color;
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

}
