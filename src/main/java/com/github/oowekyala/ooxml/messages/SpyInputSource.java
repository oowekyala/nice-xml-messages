/*
 * MIT License
 *
 * Copyright (c) 2022 Clément Fournier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.oowekyala.ooxml.messages;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.function.Supplier;

import org.apache.commons.io.input.XmlStreamReader;
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
        Reader reader;
        try {
            if (getEncoding() != null) {
                reader = new InputStreamReader(byteStream, getEncoding());
            } else {
                reader = new XmlStreamReader(byteStream);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
