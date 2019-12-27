package com.github.oowekyala.ooxml.messages.more;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.XmlException;
import com.github.oowekyala.ooxml.messages.XmlPositioner;

/**
 * Reports errors in an XML document. Implementations have a way to
 * associate nodes with their location in the document.
 */
public interface XmlErrorReporter {

    /**
     * Report a warning at the location of a node.
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
    XmlException error(Node node, String message, Object... args);


    /**
     * Report an external error at the location of a node. Whether this
     * throws the exception is an implementation detail.
     *
     * @param node XML node which owns the warning
     * @param ex   Cause exception
     */
    XmlException error(Node node, Throwable ex);


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
         * @param positioner Xml positioner
         *
         * @return A new error reporter
         */
        XmlErrorReporter create(XmlPositioner positioner);

    }


}
