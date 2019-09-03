package com.github.oowekyala.oxml;

import java.util.Objects;


class Position {

    static final Position UNDEFINED = new Position(-1, -1);
    private final int line;
    private final int column;
    private final String fileUrlOrWhatever;

    Position(int line, int column) {
        this(null, line, column);
    }

    Position(String fileUrlOrWhatever, int line, int column) {
        this.fileUrlOrWhatever = fileUrlOrWhatever;
        this.line = line;
        this.column = column;
    }

    int getLine() {
        return line;
    }

    int getColumn() {
        return column;
    }

    String getFileUrlOrWhatever() {
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
