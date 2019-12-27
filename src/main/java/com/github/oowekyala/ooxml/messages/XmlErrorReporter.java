package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;

/**
 * Reports errors in an XML document. This is meant as a helper to
 * validate XML documents.
 *
 * <p>The formatter used to template messages is implementation dependent,
 * as is the behaviour when the template argument array is null.
 */
public interface XmlErrorReporter {

    /**
     * Report a warning at the location of a node.
     *
     * @param node    XML node which owns the warning
     * @param message Message, possibly templated
     * @param args    Formatter arguments
     */
    void warn(@Nullable Node node, String message, Object... args);


    /**
     * Report an error at the location of a node. Whether this throws
     * the exception is an implementation detail.
     *
     * @param node    XML node which owns the warning
     * @param message Message, possibly templated
     * @param args    Formatter arguments
     *
     * @return An exception summarizing the error
     */
    XmlException error(@Nullable Node node, String message, Object... args);


    /**
     * Report an external error at the location of a node.
     *
     * @param node XML node which owns the warning
     * @param ex   Cause exception
     *
     * @return An exception summarizing the error
     */
    XmlException error(@Nullable Node node, Throwable ex);


    /**
     * Report an error at the location of a node. The exception is
     * thrown.
     *
     * @param node    XML node which owns the warning
     * @param message Message, possibly templated
     * @param args    Formatter arguments
     *
     * @return Never
     *
     * @throws XmlException Always
     */
    XmlException fatal(@Nullable Node node, String message, Object... args);


    /**
     * Report an external error at the location of a node. The
     * exception is thrown.
     *
     * @param node XML node which owns the warning
     * @param ex   Exception to set as the cause of the thrown exception
     *
     * @return Never
     *
     * @throws XmlException Always
     */
    XmlException fatal(@Nullable Node node, Throwable ex);


    /**
     * End the reporting phase. For example, if this reporter is configured
     * to accumulate errors, this should throw the accumulated errors. This
     * could also print some footer or anything.
     */
    void close();


}
