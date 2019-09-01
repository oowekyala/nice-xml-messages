package com.github.oowekyala.rset.xml;

import java.util.Objects;


class Position {

    private final int line;
    private final int column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
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
