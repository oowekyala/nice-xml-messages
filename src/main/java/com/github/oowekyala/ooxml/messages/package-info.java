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

/**
 * Simple API to produce nice error messages when reading an XML file.
 *
 * <p>Use {@link com.github.oowekyala.ooxml.messages.OoxmlFacade#parse(javax.xml.parsers.DocumentBuilder,
 * org.xml.sax.InputSource, com.github.oowekyala.ooxml.messages.XmlMessageHandler)
 * XmlErrorUtils::parse} to get an instance of an {@link com.github.oowekyala.ooxml.messages.XmlPositioner}.
 * This object is used to associate {@link org.w3c.dom.Node}s with their
 * location, and create message entries (under the form of an {@link com.github.oowekyala.ooxml.messages.XmlException})
 * featuring context information.
 *
 * <p>For example, if an XML parse exception occurs, instead of
 *
 * <pre>{@code
 * javax.xml.transform.TransformerException: The entity "amb" was referenced, but not declared.
 * }</pre>
 *
 * the exception has the following message:
 *
 * <pre>{@code
 * XML parsing error (in /some/file.xml)
 *     1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 *     2| <list>
 *     3|     <list foo="&amb;"/>
 *                            ^ The entity "amb" was referenced, but not declared.
 *
 *     4| </list>
 * }</pre>
 *
 * or
 *
 * <pre>{@code
 * Error at /some/file.xml:3:21 - The entity "amb" was referenced, but not declared.
 * }</pre>
 *
 * if you use {@link com.github.oowekyala.ooxml.messages.XmlPositioner#withShortMessages()}.
 *
 * <p>This API is also useful to validate an XML file after
 * it's parsed, and emit targeted error messages that come with
 * the same kind of file help. See {@link com.github.oowekyala.ooxml.messages.XmlErrorReporter}.
 *
 * <p>{@link com.github.oowekyala.ooxml.messages.XmlPositioner} is meant
 * as a low-level utility, to build helpers such as {@link com.github.oowekyala.ooxml.messages.XmlErrorReporter}.
 *
 * @author Clément Fournier
 */
package com.github.oowekyala.ooxml.messages;

