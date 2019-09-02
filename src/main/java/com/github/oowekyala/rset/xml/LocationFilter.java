package com.github.oowekyala.rset.xml;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.helpers.XMLFilterImpl;


class LocationFilter extends XMLFilterImpl {

    private static final String PREFIX = "oxml:";
    private static final String BEGIN_LINE = PREFIX + "beginLine";
    private static final String BEGIN_COLUMN = PREFIX + "beginColumn";
    private static final String BEGIN_POS = PREFIX + "beginLoc";
    private static final String END_LINE = PREFIX + "endLine";
    private static final String END_COLUMN = PREFIX + "endColumn";

    private static final String TEXT_DOC = PREFIX + "textDoc";

    private static final String NSPACE = "http://myStuff";
    private static final String LOCATION = "location";
    Locator locator;

    LocationFilter(XMLReader reader) {
        super(reader);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        String loc = locator.getSystemId() + ":" + locator.getLineNumber() + ":" + locator.getColumnNumber();
        Attributes2Impl newAttrs = new Attributes2Impl(atts);
        newAttrs.addAttribute(NSPACE, LOCATION, "oxml:location", "CDATA", loc);
        super.startElement(uri, localName, qName, newAttrs);
    }

    static Position beginPos(Node node) {
        Position pos = null;
        if (node instanceof Element) {
            Element element = (Element) node;
            pos = (Position) element.getUserData(BEGIN_POS);
        }
        // TODO support attributes
        return pos == null ? Position.UNDEFINED : pos;
    }

    static void assignLineNumbers(Element root) {
        String loc = root.getAttributeNS(NSPACE, "location");
        if (loc != null) {
            String[] split = loc.split(":");
            int col = Integer.parseInt(split[split.length - 1]);
            int line = Integer.parseInt(split[split.length - 2]);
            // TODO sysid is constant by document
            String sysId = Arrays.stream(split).limit(split.length - 2).collect(Collectors.joining());
            Position pos = new Position(sysId, col, line);
            root.setUserData(BEGIN_POS, pos, null);
            root.removeAttributeNS(NSPACE, "location");
        }

        for (int i = 0; i < root.getChildNodes().getLength(); i++) {
            Node item = root.getChildNodes().item(i);
            if (item instanceof Element) {
                assignLineNumbers((Element) item);
            }
        }
    }
}
