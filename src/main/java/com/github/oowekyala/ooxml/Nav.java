package com.github.oowekyala.ooxml;

import java.util.List;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.xpath.MinXPath;

/**
 * Adapter to support {@link MinXPath}.
 */
public interface Nav<N> {

    Nav<Node> W3C_DOM = new DomNav();


    /** {@link Node#getNodeType()} */
    short kind(N node);


    /** {@link Node#getNodeName()} */
    String name(N node);


    /** {@link Node#getParentNode()} */
    N parent(N node);


    N nextSibling(N node);


    N prevSibling(N node);


    N attribute(N element, String name);


    String attrValue(N element, String name);


    List<N> attributes(N n);


    List<N> children(N n);

}
