package com.github.oowekyala.ooxml.messages;

import java.util.Objects;


/**
 * Represents a position in a file.
 */
public class XmlPosition {

    public static final XmlPosition UNDEFINED = new XmlPosition(-1, -1);

    private final int line;
    private final int column;
    private final String systemId;

    private XmlPosition(int line, int column) {
        this(null, line, column);
    }

    public XmlPosition(String systemId, @OneBased int line, @OneBased int column) {
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    public @OneBased int getLine() {
        return line;
    }

    public @OneBased int getColumn() {
        return column;
    }

    public String getSystemId() {
        return systemId;
    }

    @Override
    public String toString() {
        return (systemId == null ? "" : "in " + systemId + ":")
            + " line " + line + ", column " + column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        XmlPosition that = (XmlPosition) o;
        return line == that.line &&
            column == that.column &&
            Objects.equals(systemId, that.systemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column, systemId);
    }
}
