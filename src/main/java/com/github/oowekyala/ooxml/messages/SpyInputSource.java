package com.github.oowekyala.ooxml.messages;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.function.Supplier;

import org.xml.sax.InputSource;

import com.github.oowekyala.ooxml.messages.InternalUtil.TeeReader;

class SpyInputSource extends InputSource {

    private Supplier<String> sup;

    public SpyInputSource(String wholeText) {
        if (wholeText != null) {
            sup = () -> wholeText;
        }
    }

    public SpyInputSource() {

    }

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
}
