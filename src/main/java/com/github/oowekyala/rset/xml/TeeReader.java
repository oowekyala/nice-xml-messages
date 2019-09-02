package com.github.oowekyala.rset.xml;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;


class TeeReader extends FilterReader {

    private final Writer copySink;

    TeeReader(Reader source, Writer sink) {
        super(source);
        this.copySink = sink;
    }


    @Override
    public int read() throws IOException {
        int result = super.read();
        this.copySink.write(result);
        return result;
    }

    @Override
    public int read(char[] b, int off, int len) throws IOException {
        int numRead = super.read(b, off, len);
        this.copySink.write(b, off, numRead); // pay attention to use "numRead" and not "len"
        return numRead;
    }

    @Override
    public int read(char[] b) throws IOException {
        int result = super.read(b);
        this.copySink.write(b);
        return result;
    }
}
