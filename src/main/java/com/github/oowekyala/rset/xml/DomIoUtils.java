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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Clément Fournier
 */
public class DomIoUtils {

    /**
     * Parse a document using the given deserializer.
     */
    static <T> T parse(InputStream inputStream, XmlSerializer<T> ser, ErrorReporter reporter) throws ParserConfigurationException, IOException, SAXException {
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
    static <T> T parse(Reader inputStream, XmlSerializer<T> ser, ErrorReporter reporter) throws ParserConfigurationException, IOException, SAXException {
        StringWriter writer = new CustomStringWriter();
        InputSource source = new InputSource(new TeeReader(inputStream, writer));
        return parse(source, writer::toString, ser, reporter);
    }


    private static <T> T parse(InputSource inputStream,
                               Supplier<String> inputCopy,
                               XmlSerializer<T> ser,
                               ErrorReporter reporter) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document parsed;
        try {
            parsed = dBuilder.parse(inputStream);
        } catch (SAXException e) {
            reporter.setDocument(inputCopy.get());
            if (e instanceof SAXParseException) {
                throw reporter.error((SAXParseException) e);
            }
            throw e;
        }

        TextDoc doc = new TextDoc(inputCopy.get());
        reporter.setDocument(inputCopy.get());

        LineNumberScanner.determineLocation(parsed, doc, 0);

        return ser.fromXml(parsed.getDocumentElement(), reporter);
    }

    public <T> Document makeDoc(T rootObj, XmlSerializer<T> ser) {
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

}
