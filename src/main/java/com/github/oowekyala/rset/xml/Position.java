package com.github.oowekyala.rset.xml;

import java.util.Objects;


class Position {

    public static final Position UNDEFINED = new Position(-1, -1);
    private final int line;
    private final int column;
    private final String fileUrlOrWhatever;

    public Position(int line, int column) {
        this(null, line, column);
    }

    public Position(String fileUrlOrWhatever, int line, int column) {
        this.fileUrlOrWhatever = fileUrlOrWhatever;
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getFileUrlOrWhatever() {
        return fileUrlOrWhatever;
    }

    @Override
    public String toString() {
        return "line " + line + ", column " + column;
    }

    @Override
    public boolean equals(Object data) {
        if (this == data) {
            return true;
        }
        if (data == null || getClass() != data.getClass()) {
            return false;
        }
        Position position = (Position) data;
        return line == position.line &&
            column == position.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column);
    }
}
