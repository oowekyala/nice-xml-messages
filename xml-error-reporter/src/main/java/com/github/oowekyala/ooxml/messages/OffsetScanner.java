/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package com.github.oowekyala.ooxml.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/*
    TODO support attribute nodes, don't do all that eagerly
 */
class OffsetScanner {

    private static final String PREFIX = "ooxml:";
    private static final String BEGIN_POS = PREFIX + "beginLoc";

    private final String systemId;

    OffsetScanner(String systemId) {
        this.systemId = systemId;
    }

    int determineLocation(Node n, TextDoc positioner, int index) {
        int nextIndex = index;
        int nodeLength = 0;
        int textLength = 0;
        String xmlString = positioner.getSourceCode();
        if (n.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
            nextIndex = xmlString.indexOf("<!DOCTYPE", nextIndex);
        } else if (n.getNodeType() == Node.COMMENT_NODE) {
            nextIndex = xmlString.indexOf("<!--", nextIndex);
        } else if (n.getNodeType() == Node.ELEMENT_NODE) {
            nextIndex = xmlString.indexOf("<" + n.getNodeName(), nextIndex);
            nodeLength = xmlString.indexOf(">", nextIndex) - nextIndex + 1;
        } else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
            nextIndex = xmlString.indexOf("<![CDATA[", nextIndex);
        } else if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            ProcessingInstruction pi = (ProcessingInstruction) n;
            nextIndex = xmlString.indexOf("<?" + pi.getTarget(), nextIndex);
        } else if (n.getNodeType() == Node.TEXT_NODE) {
            String te = unexpandEntities(n, n.getNodeValue(), true);
            int newIndex = xmlString.indexOf(te, nextIndex);
            if (newIndex == -1) {
                // try again without escaping the quotes
                te = unexpandEntities(n, n.getNodeValue(), false);
                newIndex = xmlString.indexOf(te, nextIndex);
            }
            if (newIndex > 0) {
                textLength = te.length();
                nextIndex = newIndex;
            }
        } else if (n.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            nextIndex = xmlString.indexOf("&" + n.getNodeName() + ";", nextIndex);
        }
        setBeginLocation(n, nextIndex, positioner);

        nextIndex += nodeLength;

        if (n.hasChildNodes()) {
            NodeList childs = n.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                nextIndex = determineLocation(childs.item(i), positioner, nextIndex);
            }
        }

        // autoclosing element, eg <a />
        boolean isAutoClose = !n.hasChildNodes()
            && n.getNodeType() == Node.ELEMENT_NODE
            // nextIndex is up to the closing > at this point
            && xmlString.startsWith("/>", nextIndex - 2);

        if (n.getNodeType() == Node.ELEMENT_NODE && !isAutoClose) {
            nextIndex += 2 + n.getNodeName().length() + 1; // </nodename>
        } else if (n.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
            Node nextSibling = n.getNextSibling();
            if (nextSibling.getNodeType() == Node.ELEMENT_NODE) {
                nextIndex = xmlString.indexOf("<" + nextSibling.getNodeName(), nextIndex) - 1;
            } else if (nextSibling.getNodeType() == Node.COMMENT_NODE) {
                nextIndex = xmlString.indexOf("<!--", nextIndex);
            } else {
                nextIndex = xmlString.indexOf(">", nextIndex);
            }
        } else if (n.getNodeType() == Node.COMMENT_NODE) {
            nextIndex += 4 + 3; // <!-- and -->
            nextIndex += n.getNodeValue().length();
        } else if (n.getNodeType() == Node.TEXT_NODE) {
            nextIndex += textLength;
        } else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
            nextIndex += "<![CDATA[".length() + n.getNodeValue().length() + "]]>".length();
        } else if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            ProcessingInstruction pi = (ProcessingInstruction) n;
            nextIndex += "<?".length() + pi.getTarget().length() + "?>".length() + pi.getData().length();
        }
        setEndLocation(n, nextIndex - 1, positioner);
        return nextIndex;
    }

    private void setBeginLocation(Node n, int index, TextDoc textDoc) {
        if (n != null) {
            int line = textDoc.lineNumberFromOffset(index);
            int column = textDoc.columnFromOffset(line, index);
            n.setUserData(BEGIN_POS, new FilePosition(systemId, line, column), null);
        }
    }

    public static FilePosition beginPos(Node node) {
        FilePosition bline = (FilePosition) node.getUserData(BEGIN_POS);
        return bline == null ? FilePosition.UNDEFINED : bline;
    }

    private static String unexpandEntities(Node n, String te, boolean withQuotes) {
        String result = te;
        DocumentType doctype = n.getOwnerDocument().getDoctype();
        // implicit entities
        result = result.replaceAll(Matcher.quoteReplacement("&"), "&amp;");
        result = result.replaceAll(Matcher.quoteReplacement("<"), "&lt;");
        result = result.replaceAll(Matcher.quoteReplacement(">"), "&gt;");
        if (withQuotes) {
            result = result.replaceAll(Matcher.quoteReplacement("\""), "&quot;");
            result = result.replaceAll(Matcher.quoteReplacement("'"), "&apos;");
        }

        if (doctype != null) {
            NamedNodeMap entities = doctype.getEntities();
            String internalSubset = doctype.getInternalSubset();
            if (internalSubset == null) {
                internalSubset = "";
            }
            for (int i = 0; i < entities.getLength(); i++) {
                Node item = entities.item(i);
                String entityName = item.getNodeName();
                Node firstChild = item.getFirstChild();
                if (firstChild != null) {
                    result = result.replaceAll(Matcher.quoteReplacement(firstChild.getNodeValue()),
                                               "&" + entityName + ";");
                } else {
                    Matcher m = Pattern
                        .compile(Matcher.quoteReplacement("<!ENTITY " + entityName + " ") + "[']([^']*)[']>")
                        .matcher(internalSubset);
                    if (m.find()) {
                        result = result.replaceAll(Matcher.quoteReplacement(m.group(1)), "&" + entityName + ";");
                    }
                }
            }
        }
        return result;
    }

    private static void setEndLocation(Node n, int index, TextDoc textDoc) {
        // do nothing for now
    }
}
