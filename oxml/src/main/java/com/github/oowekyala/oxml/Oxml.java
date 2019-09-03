package com.github.oowekyala.oxml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

import com.github.oowekyala.oxml.ErrorReporter.ErrorReporterFactory;
import com.github.oowekyala.oxml.Util.TeeInputStream;
import com.github.oowekyala.oxml.Util.TeeReader;

/**
 * @author Clément Fournier
 */
public class Oxml {


    private static final Oxml DEFAULT = new Oxml();

    public static  Oxml getDefault() {
        return DEFAULT;
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
    public LocationedDoc parse(InputStream inputStream, ErrorReporterFactory reporter) throws SAXException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputSource source = new InputSource(new TeeInputStream(inputStream, bos));

        Supplier<String> docMaker = () -> {
            String bytes = bos.toString();
            int end = bytes.indexOf('0');
            return (end > 0) ? bytes.substring(0, end) : bytes;
        };

        return parse(source, docMaker, reporter);
    }

    /**
     * Parse a document using the given deserializer.
     */
    public LocationedDoc parse(Reader inputStream, ErrorReporterFactory reporter) throws SAXException {
        StringWriter writer = new StringWriter();
        InputSource source = new InputSource(new TeeReader(inputStream, writer));
        return parse(source, writer::toString, reporter);
    }

    private static LocationedDoc parse(InputSource isource,
                                       Supplier<String> inputCopy,
                                       ErrorReporterFactory reporterFactory) throws SAXException {

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        LocationFilter filter = new LocationFilter(xmlReader);

        SAXSource saxSource = new SAXSource(filter, isource);

        TransformerFactory factory = TransformerFactory.newInstance();
        DOMResult domResult = new DOMResult();
        try {
            Transformer transformer = factory.newTransformer();
            transformer.setErrorListener(new TransformerErrorHandler(reporterFactory, inputCopy, () -> filter.locator));

            transformer.transform(saxSource, domResult);
        } catch (TransformerException e) {
            throw reporterFactory.create(inputCopy.get()).error(false, e);
        }

        ErrorReporter reporter = reporterFactory.create(inputCopy.get());

        Document document = (Document) domResult.getNode();
        Element root = document.getDocumentElement();
        new OffsetScanner(isource.getSystemId())
            .determineLocation(root, new TextDoc(inputCopy.get()), 0);

        return new LocationedDoc(document, reporter);
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
            updateReporter(exception).error(true, exception);

        }

        @Override
        public void error(TransformerException exception) {
            throw updateReporter(exception).error(false, exception);
        }

        @Override
        public void fatalError(TransformerException exception) {
            throw updateReporter(exception).error(false, exception);
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

    public static class LocationedDoc {

        private final Document document;
        private final ErrorReporter reporter;

        LocationedDoc(Document document, ErrorReporter reporter) {
            this.document = document;
            this.reporter = reporter;
        }

        public Document getDocument() {
            return document;
        }

        public ErrorReporter getReporter() {
            return reporter;
        }
    }
}
