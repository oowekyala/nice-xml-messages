package com.github.oowekyala.rset.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Clément Fournier
 */
public class DomReader {


    Document parse(InputStream inputStream, ErrorReporter reporter) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Document parsed;
        try {
            parsed = dBuilder.parse(new TeeInputStream(inputStream, bos));
        } catch (SAXException e) {
            if (e instanceof SAXParseException) {
                throw reporter.error((SAXParseException) e);
            }
            throw e;
        }

        String read = bos.toString();

        LineNumberScanner.determineLocation(parsed, new SourceCodePositioner(read), 0);


        return parsed;
    }


}
