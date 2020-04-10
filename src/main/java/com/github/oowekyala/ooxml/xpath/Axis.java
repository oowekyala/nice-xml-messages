package com.github.oowekyala.ooxml.xpath;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;


enum Axis {
    SELF("self", true),
    CHILD("child", true),
    ATTRIBUTE("attribute", true) {
        @Override
        public short getPrincipalNodeKind() {
            return Node.ATTRIBUTE_NODE;
        }
    },
    FOLLOWING("following", true),
    FOLLOWING_SIBLING("following-sibling", true),
    NAMESPACE("namespace", true) {
        @Override
        public short getPrincipalNodeKind() {
            return 0; // namespace node are not supported
        }
    },
    DESCENDANT("descendant", true),
    DESCENDANT_OR_SELF("descendant-or-self", true),

    // Reverse axes
    ANCESTOR("ancestor", false),
    ANCESTOR_OR_SELF("ancestor-or-self", false),
    PARENT("parent", false),
    PRECEDING("preceding", false),
    PRECEDING_SIBLING("preceding-sibling", false);


    private static final Map<String, Axis> NAMES_TO_AXES = new HashMap<>();


    static {
        for (Axis axis : values()) {
            NAMES_TO_AXES.put(axis.getAxisName(), axis);
        }
    }


    private final String name;
    private final boolean isForward;


    Axis(String name, boolean isForward) {
        this.name = name;
        this.isForward = isForward;
    }


    /**
     * Returns whether this is a forward axis or not.
     * An axis that only ever contains the context node
     * or nodes that are after the context node in document
     * order is a forward axis.
     */
    public boolean isForward() {
        return isForward;
    }


    /**
     * Returns whether this is a reverse axis or not.
     * An axis that only ever contains
     * the context node or nodes that are before the context
     * node in document order is a reverse axis.
     */
    public boolean isReverse() {
        return !isForward();
    }


    /**
     * Returns the textual name of the axis.
     */
    public String getAxisName() {
        return name;
    }


    /**
     * Returns the principal node kind of this axis.
     * Every axis has a principal node kind. If an
     * axis can contain elements, then the principal
     * node kind is element; otherwise, it is the kind
     * of nodes that the axis can contain.
     */
    public short getPrincipalNodeKind() {
        return Node.ELEMENT_NODE;
    }


    /**
     * Returns the opposite axis.
     */
    public Axis opposite() {
        return null; // FIXME
    }


    /**
     * Returns the Axis constant that has the specified axis name,
     * or null if there is none.
     *
     * @param axisName Name of the axis to look for
     */
    public static Axis fromName(String axisName) {
        return NAMES_TO_AXES.get(axisName);
    }

}
