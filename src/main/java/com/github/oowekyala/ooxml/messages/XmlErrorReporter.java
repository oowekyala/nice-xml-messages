/*
 * MIT License
 *
 * Copyright (c) 2022 Clément Fournier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.oowekyala.ooxml.messages;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;

/**
 * Reports errors in an XML document. This is meant as a helper
 * to carry around while validating an XML document. This interface
 * is the API provided to the validating code, what happens to the
 * messages is up to the implementation. Instances may wrap an
 * {@link XmlPositioner} to associate DOM nodes with a {@link XmlPosition position}
 * for better error messages.
 *
 * <p>A simple implementation is available in {@link DefaultXmlErrorReporter}.
 * Another implementation, {@link AccumulatingErrorReporter} holds
 * off the actual printing until the reporter is {@link #close() closed}.
 * Those implementations use {@link XmlMessageHandler} as a back-end
 * to render the messages.
 *
 * <p>The formatter used to template messages is implementation dependent,
 * as is the behaviour when the template argument array is null.
 */
public interface XmlErrorReporter extends AutoCloseable {

    /**
     * Report a warning at the location of a node.
     *
     * @param node    XML node which owns the warning
     * @param message Message, possibly templated
     * @param args    Formatter arguments
     * @throws IllegalArgumentException If the message is null
     */
    void warn(@Nullable Node node, String message, Object... args);


    /**
     * Report an error at the location of a node. Whether this
     * throws the exception is an implementation detail.
     *
     * @param node    XML node which owns the warning
     * @param message Message, possibly templated
     * @param args    Formatter arguments
     * @return An exception summarizing the error
     * @throws IllegalArgumentException If the message is null
     */
    XmlException error(@Nullable Node node, String message, Object... args);


    /**
     * Report an error at the location of a node. Whether this
     * throws the exception is an implementation detail.
     *
     * @param node    XML node which owns the error
     * @param ex      Cause of the error
     * @param message Message, possibly templated
     * @param args    Formatter arguments
     * @return An exception summarizing the error
     * @throws IllegalArgumentException If the message is null
     */
    XmlException error(@Nullable Node node, Throwable ex, String message, Object... args);


    /**
     * Report an external error at the location of a node.
     *
     * @param node XML node which owns the error
     * @param ex   Cause exception
     * @return An exception summarizing the error
     * @throws IllegalArgumentException If the exception is null
     */
    XmlException error(@Nullable Node node, Throwable ex);


    /**
     * Report an error at the location of a node. The exception
     * is thrown. The method is declared as if it returned the
     * exception, but it always throws. This allows you to write
     * {@code throw reporter.fatal(...)}, to tell the compiler
     * that this is an exit point.
     *
     * @param node    XML node which owns the error
     * @param message Message, possibly templated
     * @param args    Formatter arguments
     * @return Never
     * @throws XmlException             Always
     * @throws IllegalArgumentException If the message is null
     */
    XmlException fatal(@Nullable Node node, String message, Object... args);


    /**
     * Report an external error at the location of a node.
     * The exception is thrown.
     *
     * @param node XML node which owns the error
     * @param ex   Exception to set as the cause of the thrown exception
     * @return Never
     * @throws XmlException Always
     */
    XmlException fatal(@Nullable Node node, Throwable ex);


    /**
     * End the reporting phase. For example, if this reporter is configured
     * to accumulate errors, this should throw the accumulated errors. This
     * could also print some footer or something.
     */
    void close();


    /**
     * Returns an error reporter that doesn't print messages
     * (but throws exceptions). In that case, the exceptions
     * mention the provided systemId as position information,
     * if it is not null.
     *
     * @param systemId Optional system id
     * @return A new reporter
     */
    static XmlErrorReporter noop(@Nullable String systemId) {
        return new DefaultXmlErrorReporter(XmlMessageHandler.NOOP,
                                           XmlPositioner.noPositioner(systemId));
    }

}
