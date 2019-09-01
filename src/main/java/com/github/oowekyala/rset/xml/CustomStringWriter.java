package com.github.oowekyala.rset.xml;

import java.io.StringWriter;

/**
 * For some reason the dom implementation adds a lot of null characters ('0')
 * so we cut off at the first.
 */
class CustomStringWriter extends StringWriter {

    private boolean closed;

    public CustomStringWriter() {
    }

    @Override
    public void write(int c) {
        doWrite(c);
    }

    private boolean doWrite(int c) {
        if (c == 0 || closed) {
            close();
            return false;
        }
        super.write(c);
        return true;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        if (off < 0
            || off > cbuf.length
            || len < 0
            || off + len > cbuf.length
            || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0 || closed) {
            return;
        }
        for (int i = off; i < len; i++) {
            if (!doWrite(cbuf[i])) {
                return;
            }
        }
    }

    @Override
    public void close() {
        this.closed = true;
    }


}
