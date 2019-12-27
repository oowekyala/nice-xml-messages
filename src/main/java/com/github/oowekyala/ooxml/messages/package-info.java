/**
 * Simple API to produce nice error messages when reading an XML file.
 *
 * <p>Use {@link com.github.oowekyala.ooxml.messages.XmlErrorUtils#parse(javax.xml.parsers.DocumentBuilder, org.xml.sax.InputSource, com.github.oowekyala.ooxml.messages.XmlMessageHandler)
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
 * XML parsing error
 *     1| <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 *     2| <list>
 *     3|     <list foo="&amb;"/>
 *                            ^ The entity "amb" was referenced, but not declared.
 *
 *     4| </list>
 * }</pre>
 *
 * <p>{@link com.github.oowekyala.ooxml.messages.XmlPositioner} is meant
 * as a low-level utility, it is advised to use {@link com.github.oowekyala.ooxml.messages.more.XmlErrorReporter},
 * or use a positioner as a backend for your own custom validation helper.
 *
 * @author Clément Fournier
 */

package com.github.oowekyala.ooxml.messages;

