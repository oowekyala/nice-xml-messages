package com.github.oowekyala.ooxml.messages;

import java.util.Objects;

import org.xml.sax.Locator;

import com.github.oowekyala.ooxml.messages.Annots.OneBased;


/**
 * Represents the location of an XML node in a file. This is a text range,
 * located on a single line.
 */
public final class XmlPosition {

    public static final XmlPosition UNDEFINED = new XmlPosition(-1, -1);

    private final int line;
    private final int column;
    private final int length;
    private final String systemId;

    private XmlPosition(int line, int column) {
        this(null, line, column);
    }

    public XmlPosition(String systemId, @OneBased int line, @OneBased int column) {
        this.systemId = systemId;
        this.line = line;
        this.column = column;
        this.length = 0;
    }

    public XmlPosition(String systemId, @OneBased int line, @OneBased int column, int length) {
        this.systemId = systemId;
        this.line = line;
        this.column = column;
        this.length = length;
    }

    /**
     * Returns the (1-based) line number of this position.
     * If this position is undefined, the result is garbage.
     */
    public @OneBased int getLine() {
        return line;
    }

    /**
     * Returns the (1-based) column number of the start of this range.
     * If this position is undefined, the result is garbage.
     */
    public @OneBased int getColumn() {
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

    /**
     * Returns the length of the text range.
     */
    public int getLength() {
        return length;
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
     *
     * @return An undefined position
     */
    public static XmlPosition undefinedIn(String systemId) {
        return new XmlPosition(systemId, -1, -1);
    }
}
