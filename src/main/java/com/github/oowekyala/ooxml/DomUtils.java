/*
 * MIT License
 *
 * Copyright (c) 2022 Clément Fournier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.oowekyala.ooxml;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple utilities to work with the {@code org.w3c.dom} API.
 */
public final class DomUtils {

    /**
     * Returns the list of children of the given element
     * that are also elements.
     */
    public static List<Element> children(Element element) {
        List<Node> nodes = new ArrayList<>(asList(element.getChildNodes()));
        nodes.removeIf(n -> !(n instanceof Element));
        return (List) nodes;
    }


    /**
     * Returns the list of children of the given element
     * that are also elements.
     */
    public static List<Element> childrenNamed(Element element, String localName) {
        List<Node> nodes = new ArrayList<>(asList(element.getChildNodes()));
        nodes.removeIf(n -> !(n instanceof Element) || !n.getLocalName().equals(localName));
        return (List) nodes;
    }


    /**
     * Returns the value of the attribute with the given name
     * as an optional.
     *
     * @param element       Element to get the attribute from
     * @param attributeName Attribute name (local name, no NS)
     */
    public static Optional<String> getAttributeOpt(Element element, String attributeName) {
        if (element.hasAttribute(attributeName)) {
            return Optional.ofNullable(element.getAttribute(attributeName));
        }
        return Optional.empty();
    }


    /**
     * Returns an unmodifiable view of the given nodelist as a list.
     *
     * @param nodeList A node list
     * @return A list view
     */
    public static List<Node> asList(NodeList nodeList) {
        return new AbstractList<Node>() {
            @Override
            public Node get(int index) {
                return nodeList.item(index);
            }


            @Override
            public int size() {
                return nodeList.getLength();
            }
        };
    }


    /**
     * Returns an unmodifiable view of the given nodemap as a list.
     * Note that as per the specification of {@link NamedNodeMap},
     * elements are in no particular order, but this allows to
     * iterate over them easily.
     *
     * @param nodeList A node map
     * @return A list view
     */
    public static List<Node> asList(NamedNodeMap nodeList) {
        return new AbstractList<Node>() {
            @Override
            public Node get(int index) {
                return nodeList.item(index);
            }


            @Override
            public int size() {
                return nodeList.getLength();
            }
        };
    }


    /**
     * Returns a new map of nodes built from the given node map.
     * This uses local names to index the nodes. The returned map
     * is modifiable.
     *
     * @param nodeList A node map
     * @return A new, modifiable map
     */
    public static Map<String, Node> toMap(NamedNodeMap nodeList) {
        Map<String, Node> map = new HashMap<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            map.put(item.getNodeName(), item);
        }
        return map;
    }
}
