package com.github.oowekyala.ooxml.messages;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.function.Supplier;

import org.xml.sax.InputSource;

class SpyInputSource extends InputSource {

    private Supplier<String> sup;


    @Override
    public void setCharacterStream(Reader characterStream) {
        StringWriter writer = new StringWriter();
        super.setCharacterStream(new TeeReader(characterStream, writer));
        this.sup = writer::toString;
    }

    @Override
    public void setByteStream(InputStream byteStream) {
        if (byteStream == null) {
            return;
        }
        InputStreamReader reader;
        if (getEncoding() != null) {
            try {
                reader = new InputStreamReader(byteStream, getEncoding());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            reader = new InputStreamReader(byteStream);
        }

        setCharacterStream(reader);
    }


    public String getReadSoFar() {
        return sup == null ? "" : sup.get();
    }


    public void setFullText(String wholeText) {
        sup = () -> wholeText;
        super.setCharacterStream(new StringReader(wholeText));
    }


    private static class TeeReader extends FilterReader {

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
            if (numRead > 0) {
                this.copySink.write(b, off, numRead); // pay attention to use "numRead" and not "len"
            }
            return numRead;
        }


        @Override
        public int read(char[] b) throws IOException {
            int result = super.read(b);
            this.copySink.write(b);
            return result;
        }
    }
}
