package com.github.oowekyala.ooxml.messages;

import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.PARSING_ERROR;
import static com.github.oowekyala.ooxml.messages.XmlMessageKind.StdMessageKind.PARSING_WARNING;

import java.io.Reader;
import java.util.function.Supplier;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

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
     * Parses an XML document and creates an error reporter.
     * Parse exceptions thrown by the parser are reported using the given
     * reporter factory, on a best-effort basis.
     *
     * @param inputSource         Source
     * @param parsingErrorHandler Exception handler for XML parsing errors.
     *                            This is called when the
     */
    public PositionedXmlDoc parse(InputSource inputSource, XmlMessageHandler parsingErrorHandler) throws XmlException {
        return parseImpl(spyOn(inputSource), parsingErrorHandler);
    }

    /**
     * Parses an XML document and creates an error reporter.
     *
     * @param reader              Source
     * @param parsingErrorHandler Factory for error reporters
     *
     * @see #parse(InputSource, XmlMessageHandler)
     */
    public PositionedXmlDoc parse(Reader reader, XmlMessageHandler parsingErrorHandler) throws XmlException {
        return parse(new InputSource(reader), parsingErrorHandler);
    }

    private PositionedXmlDoc parseImpl(SpyInputSource isource, XmlMessageHandler handler) throws XmlException {

        final XMLReader xmlReader;
        try {
            xmlReader = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new XmlException(XmlPosition.UNDEFINED, e.getMessage(), e.getMessage(), PARSING_ERROR, e);
        }

        LocationFilter filter = new LocationFilter(xmlReader);

        SAXSource saxSource = new SAXSource(filter, isource);

        TransformerFactory factory = TransformerFactory.newInstance();
        DOMResult domResult = new DOMResult();
        try {
            Transformer transformer = factory.newTransformer();
            TransformerErrorHandler errorHandler =
                new TransformerErrorHandler(isource::getRead, () -> filter.locator, handler);
            transformer.setErrorListener(errorHandler);

            transformer.transform(saxSource, domResult);
        } catch (TransformerException e) {
            throw new PartialFilePositioner(isource.getRead())
                .createEntry(PARSING_ERROR, handler.supportsAnsiColors(), e);
        }

        Document document = (Document) domResult.getNode();

        FullFilePositioner positioner = new FullFilePositioner(isource.getSystemId(), isource.getRead(), document);

        return new PositionedXmlDoc(document, positioner);
    }

    public static XmlErrorUtils getInstance() {
        return DEFAULT;
    }


    /*
     TODO the whole string may be kept if we're parsing to DOM, for SAX
       we should only keep the head of the stream
     */
    private static SpyInputSource spyOn(InputSource inputSource) {
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

    private static class TransformerErrorHandler implements ErrorListener {

        private final Supplier<String> inputCopy;
        private final XmlMessageHandler parseExceptionHandler;
        private Supplier<Locator> locatorSupplier;

        public TransformerErrorHandler(Supplier<String> inputCopy,
                                       Supplier<Locator> locatorSupplier,
                                       XmlMessageHandler parseExceptionHandler) {
            this.inputCopy = inputCopy;
            this.locatorSupplier = locatorSupplier;
            this.parseExceptionHandler = parseExceptionHandler;
        }

        private XmlPositioner updatePositioner(TransformerException exception) {
            if (exception.getLocator() == null) {
                exception.setLocator(new LocatorAdapter(locatorSupplier.get()));
            }
            return new PartialFilePositioner(inputCopy.get());
        }

        private XmlException parseException(TransformerException exception, XmlMessageKind kind, XmlPositioner positioner) {
            return positioner.createEntry(kind, parseExceptionHandler.supportsAnsiColors(), exception);
        }

        @Override
        public void warning(TransformerException exception) {
            parseExceptionHandler.accept(parseException(exception, PARSING_WARNING, updatePositioner(exception)));
        }

        @Override
        public void error(TransformerException exception) {
            parseExceptionHandler.accept(parseException(exception, PARSING_ERROR, updatePositioner(exception)));
        }

        @Override
        public void fatalError(TransformerException exception) {
            throw parseException(exception, PARSING_ERROR, updatePositioner(exception));
        }
    }

}
