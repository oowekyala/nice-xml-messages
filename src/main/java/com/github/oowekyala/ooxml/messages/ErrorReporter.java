package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Node;

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
     * An error occurring in the parsing phase. This is called when the
     * XML document is not well-formed (eg missing closing tag).
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


    /**
     * Creates an error reporter.
     */
    interface ErrorReporterFactory {

        /**
         * Create a new error reporter from the given string
         *
         * @param xmlDocument Full string corresponding to the XML document
         *
         * @return A new error reporter
         */
        ErrorReporter create(String xmlDocument);

    }


}
