package com.github.oowekyala.oxml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Clément Fournier
 */
public class OxmlMappers extends Oxml {

    private static final OxmlMappers DEFAULT = new OxmlMappers();

    public <T> T parse(LocationedDoc doc, XmlMapper<T> mapper) {
        return mapper.fromXml(doc.getDocument().getDocumentElement(), doc.getReporter());
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

    public static OxmlMappers getDefault() {
        return DEFAULT;
    }


}
