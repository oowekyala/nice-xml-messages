package com.github.oowekyala.ooxml.messages;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Supplier;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import com.github.oowekyala.ooxml.messages.ErrorReporter.ErrorReporterFactory;

/**
 * XML utilities.
 *
 * @author Clément Fournier
 */
public class OoXml {


    private static final OoXml DEFAULT = new OoXml();

    OoXml() {}

    /**
     * Parse a document using the given deserializer.
     */
    public PositionedXmlDoc parse(InputSource inputStream, ErrorReporterFactory reporter) throws SAXException {
        return parse(spyOn(inputStream), reporter);
    }

    public void write(Document document, File outputFile) throws IOException {
        outputFile.getParentFile().mkdirs();
        write(document, Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8));
    }

    public String writeToString(Document document) throws IOException {
        StringWriter writer = new StringWriter();
        write(document, writer);
        return writer.toString();
    }

    public void write(Document document, Writer outputFile) throws IOException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            Source source = new DOMSource(document);
            Result result = new StreamResult(outputFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IOException("Failed to save settings", e);
        }
    }

    /**
     * Parse a document using the given deserializer.
     */
    public PositionedXmlDoc parse(Reader reader, ErrorReporterFactory reporter) throws SAXException {
        return parse(new InputSource(reader), reporter);
    }

    public static OoXml getDefault() {
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
            throw reporterFactory.create(isource.getRead()).parseError(false, e);
        }

        ErrorReporter reporter = reporterFactory.create(isource.getRead());

        Document document = (Document) domResult.getNode();
        Element root = document.getDocumentElement();
        new OffsetScanner(isource.getSystemId())
            .determineLocation(root, new TextDoc(isource.getRead()), 0);

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

        private ErrorReporter updateReporter(TransformerException exception) {
            if (exception.getLocator() == null) {
                exception.setLocator(new LocatorAdapter(locatorSupplier.get()));
            }
            return reporter.create(inputCopy.get());
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
