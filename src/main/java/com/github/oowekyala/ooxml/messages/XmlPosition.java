package com.github.oowekyala.ooxml.messages;

import java.util.Objects;

import org.xml.sax.Locator;


/**
 * Represents the location of an XML node in a file.
 */
public final class XmlPosition {

    public static final XmlPosition UNDEFINED = new XmlPosition(-1, -1);

    private final int line;
    private final int column;
    private final String systemId;

    private XmlPosition(int line, int column) {
        this(null, line, column);
    }

    public XmlPosition(String systemId, @Annots.OneBased int line, @Annots.OneBased int column) {
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the (1-based) line number of the position.
     * If this position is undefined, the result is garbage.
     */
    public @Annots.OneBased int getLine() {
        return line;
    }

    /**
     * Returns the (1-based) column number of the position.
     * If this position is undefined, the result is garbage.
     */
    public @Annots.OneBased int getColumn() {
        return column;
    }

    /**
     * The system ID of the file where the node is located.
     *
     * @see Locator#getSystemId()
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * If true, column and line numbers are unreliable
     */
    public boolean isUndefined() {
        return line < 0 || column < 0;
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

    /**
     * Returns an undefined position in a document identified by the
     * given system ID.
     *
     * @param systemId System ID
     */
    public static XmlPosition undefinedIn(String systemId) {
        return new XmlPosition(systemId, -1, -1);
    }
}
