package com.github.oowekyala.ooxml;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public final class DomUtils {


    public static Map<String, Node> toMap(NamedNodeMap nodeList) {
        Map<String, Node> map = new HashMap<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            map.put(item.getNodeName(), item);
        }
        return map;
    }


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
}
