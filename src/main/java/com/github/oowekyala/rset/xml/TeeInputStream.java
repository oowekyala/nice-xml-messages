package com.github.oowekyala.rset.xml;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


class TeeInputStream extends FilterInputStream {

    private final OutputStream copySink;

    TeeInputStream(InputStream source, OutputStream sink) {
        super(source);
        this.copySink = sink;
    }

    public int read() throws IOException {
        int result = super.read();
        this.copySink.write(result);
        return result;
    }


    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        this.copySink.write(b, off, len);
        return result;
    }

    public int read(byte[] b) throws IOException {
        int result = super.read(b);
        this.copySink.write(b);
        return result;
    }
}
