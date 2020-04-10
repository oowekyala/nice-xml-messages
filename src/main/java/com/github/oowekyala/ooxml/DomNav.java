package com.github.oowekyala.ooxml;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;

class DomNav implements Nav<Node> {

    @Override
    public short kind(Node node) {
        return node.getNodeType();
    }


    @Override
    public String name(Node node) {
        return node.getNodeName();
    }


    @Override
    public Node parent(Node node) {
        return node.getParentNode();
    }

    @Override
    public Node prevSibling(Node node) {
        return node.getPreviousSibling();
    }


    @Override
    public Node nextSibling(Node node) {
        return node.getNextSibling();
    }


    @Override
    public Node attribute(Node node, String name) {
        if (!node.hasAttributes()) {
            return null;
        }
        return node.getAttributes().getNamedItem(name);
    }


    @Override
    public String attrValue(Node element, String name) {
        Node attr = attribute(element, name);
        return attr == null ? null : attr.getNodeValue();
    }


    @Override
    public List<Node> attributes(Node node) {
        if (!node.hasAttributes()) {
            return Collections.emptyList();
        }
        return DomUtils.asList(node.getAttributes());
    }


    @Override
    public List<Node> children(Node node) {
        return DomUtils.asList(node.getChildNodes());
    }
}
