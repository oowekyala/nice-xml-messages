package com.github.oowekyala.ooxml.messages;

import java.util.Objects;


/**
 * Represents a position in a file.
 */
class FilePosition {

    static final FilePosition UNDEFINED = new FilePosition(-1, -1);
    private final int line;
    private final int column;
    private final String fileUrlOrWhatever;

    private FilePosition(int line, int column) {
        this(null, line, column);
    }

    FilePosition(String fileUrlOrWhatever, @InternalUtil.OneBased int line, @InternalUtil.OneBased int column) {
        this.fileUrlOrWhatever = fileUrlOrWhatever;
        this.line = line;
        this.column = column;
    }

    @InternalUtil.OneBased int getLine() {
        return line;
    }

    @InternalUtil.OneBased int getColumn() {
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
        FilePosition position = (FilePosition) data;
        return line == position.line &&
            column == position.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column);
    }
}
