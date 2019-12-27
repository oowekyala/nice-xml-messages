/**
 * Simple API to produce nice error messages when reading an XML file.
 *
 * <p>Use {@link com.github.oowekyala.ooxml.messages.XmlErrorUtils#parse(org.xml.sax.InputSource, XmlErrorReporter.ErrorReporterFactory) XmlErrorUtils::parse}
 * and overloads to get an instance of an {@link com.github.oowekyala.ooxml.messages.XmlErrorReporter}.
 * The default produces nice error messages, for example instead of
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
 *
 * <p>The {@link com.github.oowekyala.ooxml.messages.XmlErrorReporter} can
 * be used to report an error against any {@link org.w3c.dom.Node}, which
 * is useful to enrich diagnostics eg when validating an XML config file.
 *
 *
 * <p>Key pieces of the API are {@link com.github.oowekyala.ooxml.messages.XmlErrorReporter},
 * with a default implementation in {@link com.github.oowekyala.ooxml.messages.DefaultXmlErrorReporter},
 * which can be parameterized with a custom {@link com.github.oowekyala.ooxml.messages.MessagePrinter}.
 *
 *
 * @author Clément Fournier
 */

package com.github.oowekyala.ooxml.messages;
