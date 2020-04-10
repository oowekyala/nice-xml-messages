package com.github.oowekyala.ooxml;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple utilities to work with the {@code org.w3c.dom} API.
 */
public final class DomUtils {

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
     * This uses local names.
     *
     * @param nodeList A node map
     * @return A list view
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
