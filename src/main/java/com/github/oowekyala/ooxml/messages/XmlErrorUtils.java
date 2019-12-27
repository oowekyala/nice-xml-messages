package com.github.oowekyala.ooxml.messages;

import static com.github.oowekyala.ooxml.messages.Severity.ERROR;
import static com.github.oowekyala.ooxml.messages.Severity.FATAL;
import static com.github.oowekyala.ooxml.messages.Severity.WARNING;
import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.PARSING;
import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.SCHEMA_VALIDATION;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.SourceLocator;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Main entry point of the API.
 *
 * @author Clément Fournier
 */
public class XmlErrorUtils {


    private static final XmlErrorUtils DEFAULT = new XmlErrorUtils();

    XmlErrorUtils() {
    }

    /**
     * Parses an XML document and creates an associated {@link XmlPositioner}.
     * Exceptions thrown by the parser (because of eg invalid XML syntax)
     * are reported using the given message handler. Their position is
     * recovered on a best-effort basis. Only fatal parsing exceptions
     * are thrown, and only non-fatal exceptions and warnings are handled
     * by the parameter.
     *
     * <p>To validate the document against a schema, you must use either
     * {@link DocumentBuilderFactory#setValidating(boolean)} or
     * {@link DocumentBuilderFactory#setSchema(Schema)}. Text positions
     * cannot be recovered by a {@link Validator} after the fact (this
     * is a limitation of {@code java.xml}). Note that setting both may
     * result in duplicate messages.
     *
     * <p>For best messages please back your {@link InputSource} with an
     * {@link InputStream}, or better, a {@link Reader}.
     *
     * @param domBuilder          Preconfigured DOM builder, the {@linkplain DocumentBuilder#setErrorHandler(ErrorHandler)
     *                            error handler} is set by this method.
     * @param inputSource         Source for the XML document. The {@linkplain InputSource#setSystemId(String) system
     *                            ID} should be set for better error messages.
     * @param parsingErrorHandler Exception handler for recoverable parsing or
     *                            schema validation errors.
     */
    public PositionedXmlDoc parse(DocumentBuilder domBuilder, InputSource inputSource, XmlMessageHandler parsingErrorHandler) throws XmlException, IOException {
        return parseImpl(domBuilder, spyOn(inputSource), parsingErrorHandler);
    }

    private PositionedXmlDoc parseImpl(DocumentBuilder builder, SpyInputSource isource, XmlMessageHandler handler) throws XmlException, IOException {

        builder.setErrorHandler(new MyErrorHandler(handler) {
            @Override
            XmlPositioner getPositioner() {
                return new PartialFilePositioner(isource.getReadSoFar());
            }
        });

        try {
            Document doc = builder.parse(isource);
            FullFilePositioner positioner = new FullFilePositioner(isource.getSystemId(), isource.getReadSoFar(), doc);

            return new PositionedXmlDoc(doc, positioner);
        } catch (SAXException e) {
            PartialFilePositioner positioner = new PartialFilePositioner(isource.getReadSoFar());
            throw InternalUtil.createEntryBestEffort(positioner, PARSING, FATAL, handler.supportsAnsiColors(), e);
        }
    }

    /** Returns the singleton. */
    public static XmlErrorUtils getInstance() {
        return DEFAULT;
    }


    /*
     TODO the whole string may be kept if we're parsing to DOM, for SAX
       we should only keep the head of the stream
     */
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

    private static class LocatorAdapter implements SourceLocator {

        private final Locator locator;

        private LocatorAdapter(Locator locator) {
            this.locator = locator;
        }

        @Override
        public String getPublicId() {
            return locator.getPublicId();
        }

        @Override
        public String getSystemId() {
            return locator.getSystemId();
        }

        @Override
        public int getLineNumber() {
            return locator.getLineNumber();
        }

        @Override
        public int getColumnNumber() {
            return locator.getColumnNumber();
        }
    }

    /** Transparent filter just to get a locator instance. */
    private static class LocationFilter extends XMLFilterImpl {

        Locator locator;

        LocationFilter(XMLReader reader) {
            super(reader);
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            super.setDocumentLocator(locator);
            this.locator = locator;
        }
    }

    private static abstract class MyErrorHandler implements ErrorHandler {

        private final XmlMessageHandler parseExceptionHandler;

        public MyErrorHandler(XmlMessageHandler parseExceptionHandler) {
            this.parseExceptionHandler = parseExceptionHandler;
        }

        abstract XmlPositioner getPositioner();

        private XmlException parseException(SAXParseException exception, Severity severity) {
            return InternalUtil.createEntryBestEffort(getPositioner(), SCHEMA_VALIDATION, severity, parseExceptionHandler.supportsAnsiColors(), exception);
        }

        @Override
        public void warning(SAXParseException exception) {
            parseExceptionHandler.accept(parseException(exception, WARNING));
        }

        @Override
        public void error(SAXParseException exception) {
            parseExceptionHandler.accept(parseException(exception, ERROR));
        }

        @Override
        public void fatalError(SAXParseException exception) {
            throw parseException(exception, FATAL);
        }
    }

}
