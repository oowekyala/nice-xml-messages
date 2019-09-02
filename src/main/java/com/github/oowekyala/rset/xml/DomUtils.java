package com.github.oowekyala.rset.xml;

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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.xml.sax.helpers.XMLReaderFactory;

import com.github.oowekyala.rset.xml.Util.TeeInputStream;
import com.github.oowekyala.rset.xml.Util.TeeReader;

/**
 * @author Clément Fournier
 */
public class DomUtils {

    /**
     * Parse a document using the given deserializer.
     */
    static <T> T parse(InputStream inputStream, XmlMapper<T> ser, ErrorReporter reporter) throws SAXException, TransformerException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputSource source = new InputSource(new TeeInputStream(inputStream, bos));

        Supplier<String> docMaker = () -> {
            String bytes = bos.toString();
            int end = bytes.indexOf('0');
            return (end > 0) ? bytes.substring(0, end) : bytes;
        };

        return parse(source, docMaker, ser, reporter);
    }

    /**
     * Parse a document using the given deserializer.
     */
    static <T> T parse(Reader inputStream, XmlMapper<T> ser, ErrorReporter reporter) throws SAXException, TransformerException {
        StringWriter writer = new StringWriter();
        InputSource source = new InputSource(new TeeReader(inputStream, writer));
        return parse(source, writer::toString, ser, reporter);
    }


    private static <T> T parse(InputSource isource,
                               Supplier<String> inputCopy,
                               XmlMapper<T> ser,
                               ErrorReporter reporter) throws SAXException {

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        LocationFilter filter = new LocationFilter(xmlReader);

        SAXSource saxSource = new SAXSource(filter, isource);

        TransformerFactory factory = TransformerFactory.newInstance();
        DOMResult domResult = new DOMResult();
        try {
            Transformer transformer = factory.newTransformer();
            transformer.setErrorListener(new TransformerErrorHandler(reporter, inputCopy, filter));

            transformer.transform(saxSource, domResult);
        } catch (TransformerException e) {
            throw reporter.error(false, e);
        }

        reporter.setDocument(inputCopy.get());

        Element root = ((Document) domResult.getNode()).getDocumentElement();
        LocationFilter.assignLineNumbers(root);
        return ser.fromXml(root, reporter);
    }


    public <T> Document makeDoc(T rootObj, XmlMapper<T> ser) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to create settings document builder", e);
        }
        Document document = documentBuilder.newDocument();

        Element rootElt = document.createElement(ser.eltName(rootObj));
        document.appendChild(rootElt);
        ser.toXml(rootElt, rootObj, document::createElement);

        return document;
    }


    public static void write(Document document, File outputFile) throws IOException {
        outputFile.getParentFile().mkdirs();
        write(document, Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8));
    }

    public static String writeToString(Document document) throws IOException {
        StringWriter writer = new StringWriter();
        write(document, writer);
        return writer.toString();
    }

    public static void write(Document document, Writer outputFile) throws IOException {
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

    private static class TransformerErrorHandler implements ErrorListener {

        private final ErrorReporter reporter;
        private final Supplier<String> inputCopy;
        private final LocationFilter filter;

        public TransformerErrorHandler(ErrorReporter reporter, Supplier<String> inputCopy, LocationFilter filter) {
            this.reporter = reporter;
            this.inputCopy = inputCopy;
            this.filter = filter;
        }

        private void updateDoc(TransformerException exception) {
            reporter.setDocument(inputCopy.get());
            if (filter.locator != null) {
                exception.setLocator(new LocatorAdapter(filter.locator));
            }
        }

        @Override
        public void warning(TransformerException exception) {
            updateDoc(exception);
            reporter.error(true, exception);

        }

        @Override
        public void error(TransformerException exception) {
            updateDoc(exception);
            throw reporter.error(false, exception);

        }

        @Override
        public void fatalError(TransformerException exception) {
            updateDoc(exception);
            throw reporter.error(false, exception);
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
}