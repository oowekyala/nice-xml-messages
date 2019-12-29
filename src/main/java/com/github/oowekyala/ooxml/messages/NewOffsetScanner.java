/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package com.github.oowekyala.ooxml.messages;

import static com.github.oowekyala.ooxml.messages.Annots.ZeroBased;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

class NewOffsetScanner {

    /*
        START:
        For some node, search start is
        - END of previous sibling
        - if prev sibling is null, start offset of parent content (eg closing > for elt header)
        - adjust for ignorable whitespace

        END:
        - search start is END of last child


     */

    private static final String PREFIX = "ooxml:";
    private static final String START_OFFSET = PREFIX + "startOffset";
    private static final String END_OFFSET = PREFIX + "endOffset";
    private static final String CONTENT_START_OFFSET = PREFIX + "contentStartOffset";

    private static final Pattern QUOTES = Pattern.compile("[\"']");
    private static final UserDataHandler NO_DATA_HANDLER = (operation, key, data1, src, dst) -> {};
    private final String systemId;
    private final TextDoc textDoc;
    private final String fullText;

    NewOffsetScanner(String systemId, TextDoc textDoc) {
        this.systemId = systemId;
        this.textDoc = textDoc;
        fullText = textDoc.getTextString();
    }

    private int indexOf(String s, int start) {
        if (start < 0 || start >= fullText.length()) {
            return -1;
        }
        return fullText.indexOf(s, start);
    }

    private int indexOf(char c, int start) {
        if (start < 0 || start >= fullText.length()) {
            return -1;
        }
        return fullText.indexOf(c, start);
    }

    private int indexOf(Pattern c, int start) {
        if (start < 0 || start >= fullText.length()) {
            return -1;
        }
        Matcher matcher = c.matcher(fullText).region(start, fullText.length());
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    private int endIdxOf(String target, int start) {
        int targetStart = target.length() == 1 ? indexOf(target.charAt(0), start)
                                               : indexOf(target, start);
        return addOffset(targetStart, target.length());
    }

    private int addOffset(int base, int diff) {
        if (base < 0) {
            return -1;
        }
        return base + diff;
    }

    private int startOffset(Node n) {
        return getOrCompute(n, START_OFFSET, this::startOffsetImpl);
    }

    private int endOffset(Node n) {
        return getOrCompute(n, END_OFFSET, this::endOffsetImpl);
    }

    private int contentStartOffset(Node n) {
        return getOrCompute(n, CONTENT_START_OFFSET, this::contentStartOffsetImpl);
    }

    private int getOrCompute(Node n, String key, Function<Node, Integer> compute) {
        Object data = n.getUserData(key);
        if (data instanceof Integer) {
            return (int) data;
        } else {
            Integer i = compute.apply(n);
            n.setUserData(key, i, NO_DATA_HANDLER);
            return i;
        }
    }

    private int startOffsetImpl(Node n) {
        if (n.getNodeType() == Node.DOCUMENT_NODE) {
            return fullText.isEmpty() ? -1 : 0;
        }

        Node prev = n.getPreviousSibling();

        final int start;
        if (prev != null) {
            start = endOffset(prev);
        } else if (n.getParentNode() != null) {
            start = contentStartOffset(n.getParentNode()); // doesn't account for Entity nodes
        } else if (n instanceof Attr) {
            start = startOffset(((Attr) n).getOwnerElement());
        } else {
            start = -1;
        }

        if (start < 0) {
            return -1;
        }

        switch (n.getNodeType()) {
        case Node.CDATA_SECTION_NODE:
        case Node.COMMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
        case Node.ELEMENT_NODE:
        case Node.PROCESSING_INSTRUCTION_NODE:
            return indexOf('<', start);
        case Node.ATTRIBUTE_NODE:
            return attributeOffset((Attr) n, start);
        case Node.ENTITY_REFERENCE_NODE:
            return indexOf('&', start);
        case Node.TEXT_NODE:
            return start; // TODO adjust for insignificant whitespace

        default:
            throw new IllegalStateException("Unhandled node type " + n.getNodeType() + " (" + n + ")");
        }
    }


    private int attributeOffset(Attr attr, int startOffset) {
        assert startOffset >= 0;

        String textString = fullText;
        int searchEnd = textString.indexOf('>', startOffset);

        Matcher matcher = Pattern.compile(attr.getName() + "\\s*=")
                                 .matcher(textString)
                                 .region(startOffset, searchEnd);

        if (matcher.find()) {
            return matcher.start();
        }
        return startOffset;
    }

    private int endOffsetImpl(Node n) {

        switch (n.getNodeType()) {

        case Node.COMMENT_NODE:
            return endIdxOf("-->", startOffset(n));
        case Node.CDATA_SECTION_NODE:
            return endIdxOf("]]>", startOffset(n));
        case Node.TEXT_NODE:
            return textEnd((Text) n);
        case Node.DOCUMENT_NODE:
            return fullText.length();
        case Node.ENTITY_REFERENCE_NODE:
            return endIdxOf(";", startOffset(n));
        case Node.DOCUMENT_TYPE_NODE:
            return endIdxOf("]>", startOffset(n));

        case Node.ELEMENT_NODE:
            Node last = n.getLastChild();
            if (last != null) {
                return endIdxOf(">", endOffset(last));
            } else {
                // no child
                int content = contentStartOffset(n);
                if (content < 0) {
                    return content;
                }
                if (content < 2) {
                    return endIdxOf(">", content); // there can't be text or anything
                }

                if (fullText.charAt(content - 2) == '/') {
                    // ends with "/>", ie autoclose
                    return content;
                }
            }

        default:
            throw new IllegalStateException("Unhandled node type " + n.getNodeType() + " (" + n + ")");
        }


    }

    private int textEnd(Text n) {
        int start = startOffset(n);
        String text = n.getNodeValue();

        boolean inCdata = false;
        int realLength = text.length();

        for (int i = start;
             i < fullText.length() && i < realLength;
        ) {

            if (!inCdata && fullText.charAt(i) == '&') {
                int refEnd = fullText.indexOf(';', i);
                assert refEnd > 0 : "Unclosed entity reference! This shouldn't have parsed!";
                // &amp;
                // ^   ^
                // i   refEnd
                // 0   4
                realLength += refEnd - i - 1; // the ref is replaced with a single char
                i = refEnd + 1;
            } else if (!inCdata && fullText.startsWith("<![CDATA[", i)) {
                inCdata = true;
                i += "<![CDATA[".length();
                realLength += "<![CDATA[".length();
            } else if (inCdata && fullText.startsWith("]]>", i)) {
                inCdata = false;
                i += "]]>".length();
                realLength += "]]>".length();
            } else {
                i++;
            }
        }

        return start + realLength;
    }


    private int contentStartOffsetImpl(Node n) {
        switch (n.getNodeType()) {
        case Node.DOCUMENT_NODE: {

            int firstLt = fullText.indexOf('<');
            if (firstLt < 0) {
                return firstLt;
            }

            // skip xml decl
            if (fullText.startsWith("<?xml", firstLt)) {
                return endIdxOf(">", firstLt);
            } else {
                return fullText.isEmpty() ? -1 : 0;
            }
        }

        case Node.ELEMENT_NODE: {
            int lt = indexOf('>', startOffset(n));
            return addOffset(lt, 1);
        }

        case Node.DOCUMENT_TYPE_NODE: {
            return addOffset(startOffset(n), "<!DOCTYPE".length());
        }

        case Node.COMMENT_NODE: {
            return addOffset(startOffset(n), "<!--".length());
        }

        case Node.PROCESSING_INSTRUCTION_NODE: {
            int start = startOffset(n);
            if (start < 0) {
                return start;
            }
            return start + "<?".length() + ((ProcessingInstruction) n).getTarget().length() + 1;
        }
        case Node.ATTRIBUTE_NODE:
            int quoteIdx = indexOf(QUOTES, startOffset(n));
            return addOffset(quoteIdx, 1);


        // leaves: Text, EntityReference
        default:
            throw new IllegalStateException("Leaf node cannot have content");
        }
    }


    public XmlPosition beginPos(Node node) {
        @ZeroBased
        int offset = startOffset(node);
        if (offset < 0) {
            return XmlPosition.undefinedIn(systemId);
        }
        int line = textDoc.lineNumberFromOffset(offset);
        int column = textDoc.columnFromOffset(line, offset);
        return new XmlPosition(systemId, line, column, length(node));
    }

    private int length(Node node) {
        if (node instanceof Attr) {
            return ((Attr) node).getName().length();
        } else if (node instanceof Element) {
            return ((Element) node).getTagName().length() + 1; // + '<'
        }

        return 0;
    }


}
