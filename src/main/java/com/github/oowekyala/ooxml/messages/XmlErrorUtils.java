package com.github.oowekyala.ooxml.messages;

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

import com.github.oowekyala.ooxml.messages.XmlErrorReporter.ErrorReporterFactory;

/**
 * Main entry point of the API.
 *
 * @author Clément Fournier
 */
public class XmlErrorUtils {


    private static final XmlErrorUtils DEFAULT = new XmlErrorUtils();

    XmlErrorUtils() {}

    /**
     * Parses an XML document and creates an error reporter.
     * Parse exceptions thrown by the parser are reported using the given
     * reporter factory, on a best-effort basis.
     *
     * @param inputSource     Source
     * @param reporterFactory Factory for error reporters
     */
    public PositionedXmlDoc parse(InputSource inputSource, ErrorReporterFactory reporterFactory) throws SAXException {
        return parse(spyOn(inputSource), reporterFactory);
    }

    /**
     * Parses an XML document and creates an error reporter.
     *
     * @param reader          Source
     * @param reporterFactory Factory for error reporters
     *
     * @see #parse(InputSource, ErrorReporterFactory)
     */
    public PositionedXmlDoc parse(Reader reader, ErrorReporterFactory reporterFactory) throws SAXException {
        return parse(new InputSource(reader), reporterFactory);
    }

    /**
     * Parses an XML document and creates an error reporter.
     * This uses a default reporter factory.
     *
     * @see #parse(InputSource, ErrorReporterFactory)
     */
    public PositionedXmlDoc parse(Reader reader) throws SAXException {
        return parse(new InputSource(reader), x -> new DefaultXmlErrorReporter(MessagePrinter.SYSTEM_ERR, x));
    }

    public static XmlErrorUtils getDefault() {
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

    private static PositionedXmlDoc parse(SpyInputSource isource,
                                          ErrorReporterFactory reporterFactory) throws SAXException {

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        LocationFilter filter = new LocationFilter(xmlReader);

        SAXSource saxSource = new SAXSource(filter, isource);

        TransformerFactory factory = TransformerFactory.newInstance();
        DOMResult domResult = new DOMResult();
        try {
            Transformer transformer = factory.newTransformer();
            transformer.setErrorListener(new TransformerErrorHandler(reporterFactory, isource::getRead, () -> filter.locator));

            transformer.transform(saxSource, domResult);
        } catch (TransformerException e) {
            throw reporterFactory.create(new PartialFilePositioner(isource.getRead()))
                                 .parseError(false, e);
        }

        Document document = (Document) domResult.getNode();

        FullFilePositioner positioner = new FullFilePositioner(isource.getSystemId(), isource.getRead(), document);

        XmlErrorReporter reporter = reporterFactory.create(positioner);

        return new PositionedXmlDoc(document, reporter);
    }

    private static class TransformerErrorHandler implements ErrorListener {

        private final ErrorReporterFactory reporter;
        private final Supplier<String> inputCopy;
        private Supplier<Locator> locatorSupplier;

        public TransformerErrorHandler(ErrorReporterFactory reporter,
                                       Supplier<String> inputCopy,
                                       Supplier<Locator> locatorSupplier) {
            this.reporter = reporter;
            this.inputCopy = inputCopy;
            this.locatorSupplier = locatorSupplier;
        }

        private XmlErrorReporter updateReporter(TransformerException exception) {
            if (exception.getLocator() == null) {
                exception.setLocator(new LocatorAdapter(locatorSupplier.get()));
            }
            return reporter.create(new PartialFilePositioner(inputCopy.get()));
        }

        @Override
        public void warning(TransformerException exception) {
            updateReporter(exception).parseError(true, exception);

        }

        @Override
        public void error(TransformerException exception) {
            throw updateReporter(exception).parseError(false, exception);
        }

        @Override
        public void fatalError(TransformerException exception) {
            throw updateReporter(exception).parseError(false, exception);
        }
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

}
