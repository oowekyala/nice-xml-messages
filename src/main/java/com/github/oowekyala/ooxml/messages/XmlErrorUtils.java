package com.github.oowekyala.ooxml.messages;

import static com.github.oowekyala.ooxml.messages.Severity.ERROR;
import static com.github.oowekyala.ooxml.messages.Severity.FATAL;
import static com.github.oowekyala.ooxml.messages.Severity.WARNING;
import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.PARSING;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Main entry point of the API.
 *
 * @author Clément Fournier
 */
public final class XmlErrorUtils {


    private static final XmlErrorUtils DEFAULT = new XmlErrorUtils();

    XmlErrorUtils() {
    }

    /**
     * Parses an XML document and creates an associated {@link XmlPositioner}.
     * Exceptions thrown by the parser (eg because of invalid XML syntax)
     * are reported using the given message handler. Their position is
     * recovered on a best-effort basis. Only fatal parsing exceptions
     * are thrown, but even them are passed to the given {@link XmlMessageHandler}.
     *
     * <p>To validate the document against a schema, you must use either
     * {@link DocumentBuilderFactory#setValidating(boolean) setValidating} or
     * {@link DocumentBuilderFactory#setSchema(Schema) setSchema} on the
     * document builder factory. Text positions cannot be recovered by a
     * {@link Validator} after the fact (this is a limitation of {@code java.xml}).
     * Note that setting both may result in duplicate messages.
     *
     * <p>For best messages back your {@link InputSource} with an
     * {@link InputStream}, or better, a {@link Reader}.
     *
     * @param domBuilder          Preconfigured DOM builder, the {@linkplain DocumentBuilder#setErrorHandler(ErrorHandler)
     *                            error handler} is set by this method.
     * @param inputSource         Source for the XML document. The {@linkplain InputSource#setSystemId(String) system
     *                            ID} should be set for better error messages.
     * @param parsingErrorHandler Exception handler for recoverable parsing or
     *                            schema validation errors.
     *
     * @throws IOException  If reading from the input source throws an IOException
     * @throws XmlException If the parser throws a fatal exception
     */
    public PositionedXmlDoc parse(DocumentBuilder domBuilder,
                                  InputSource inputSource,
                                  XmlMessageHandler parsingErrorHandler) throws XmlException, IOException {
        return parseImpl(domBuilder, spyOn(inputSource), parsingErrorHandler);
    }

    private PositionedXmlDoc parseImpl(DocumentBuilder builder, SpyInputSource isource, XmlMessageHandler handler) throws XmlException, IOException {

        builder.setErrorHandler(new MyErrorHandler(handler) {
            @Override
            XmlPositioner getPositioner() {
                return new PartialFilePositioner(isource.getReadSoFar(), isource.getSystemId());
            }
        });

        try {
            Document doc = builder.parse(isource);
            FullFilePositioner positioner = new FullFilePositioner(isource.getReadSoFar(), isource.getSystemId());

            return new PositionedXmlDoc(doc, positioner);
        } catch (SAXException e) {
            PartialFilePositioner positioner = new PartialFilePositioner(isource.getReadSoFar(), isource.getSystemId());
            XmlException ex = InternalUtil.createEntryBestEffort(positioner, PARSING, FATAL, handler.supportsAnsiColors(), e);
            handler.accept(ex);
            throw ex;
        }
    }

    /** Returns the singleton. */
    public static XmlErrorUtils getInstance() {
        return DEFAULT;
    }


    private static SpyInputSource spyOn(InputSource inputSource) throws IOException {
        SpyInputSource is = new SpyInputSource();
        is.setSystemId(inputSource.getSystemId());
        is.setPublicId(inputSource.getPublicId());
        is.setEncoding(is.getEncoding());
        if (inputSource.getCharacterStream() != null) {
            is.setCharacterStream(inputSource.getCharacterStream());
        } else {
            if (inputSource.getByteStream() != null) {
                is.setByteStream(inputSource.getByteStream());
            }
        }

        if (is.getCharacterStream() != null) {
            is.setFullText(InternalUtil.readFully(is.getCharacterStream()));
        }

        return is;
    }

    private static abstract class MyErrorHandler implements ErrorHandler {

        private final XmlMessageHandler handler;

        public MyErrorHandler(XmlMessageHandler handler) {
            this.handler = handler;
        }

        abstract XmlPositioner getPositioner();

        private XmlException parseException(SAXParseException exception, Severity severity) {
            return InternalUtil.createEntryBestEffort(getPositioner(), PARSING, severity, handler.supportsAnsiColors(), exception);
        }

        @Override
        public void warning(SAXParseException exception) {
            handler.accept(parseException(exception, WARNING));
        }

        @Override
        public void error(SAXParseException exception) {
            handler.accept(parseException(exception, ERROR));
        }

        @Override
        public void fatalError(SAXParseException exception) {
            XmlException ex = parseException(exception, FATAL);
            handler.accept(ex);
            throw ex;
        }
    }

}
