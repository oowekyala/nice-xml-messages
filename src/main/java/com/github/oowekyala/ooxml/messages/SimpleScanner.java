package com.github.oowekyala.ooxml.messages;


import java.util.ArrayDeque;
import java.util.Deque;

class SimpleScanner {

    protected final char[] chars;
    protected final int start;
    protected final int end;

    private final Deque<String> stringStack = new ArrayDeque<>(1);


    SimpleScanner(String descriptor) {
        if (descriptor == null || descriptor.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot scan \"" + descriptor + "\"");
        }

        descriptor = descriptor.trim();

        this.chars = new char[descriptor.length() + 1]; // Add one last entry (\0) to not overflow on EOI
        this.start = 0;
        this.end = descriptor.length();
        descriptor.getChars(start, end, chars, 0);
    }

    public char charAt(int off) {
        return chars[off];
    }

    public boolean isInRange(int off) {
        return off < end;
    }

    public int consumeChar(int start, char l, String s) {
        if (chars[start] != l) {
            throw expected(s, start);
        }
        return start + 1;
    }

    public int consumeChar(int start, char l) {
        return consumeChar(start, l, "" + l);
    }


    public boolean isDigit(int off) {
        return Character.isDigit(charAt(off));
    }

    boolean isIdentStart(int off) {
        return Character.isJavaIdentifierStart(charAt(off));
    }

    boolean isIdentChar(int off) {
        return Character.isJavaIdentifierPart(charAt(off));
    }

    public int skipWhitespace(final int start) {
        int cur = start;
        while (isInRange(cur) && Character.isWhitespace(charAt(cur))) {
            cur++;
        }
        return cur;
    }

    public RuntimeException expected(String expectedWhat, int pos) {
        String prefix = "Parse exception in:  ";
        String messageLine = InternalUtil.buildCaretLine(
            "Expected " + expectedWhat,
            prefix.length() + pos - start,
            1
        );
        String fullMessage = prefix + bufferToString() + "\n" + messageLine + "\n";
        return new IllegalArgumentException(fullMessage) {
            @Override
            public String toString() {
                return getMessage();
            }
        };
    }


    public void expectEoI(int e) {
        consumeChar(e, (char) 0, "end of input");
    }


    public String bufferToString() {
        return bufferToString(start, end);
    }


    public String bufferToString(int start, int end) {
        if (start == end) {
            return "";
        }
        return new String(chars, start, end - start);
    }


    void pushStr(String p) {
        stringStack.push(p);
    }

    String popStr() {
        return stringStack.removeFirst();
    }

    @Override
    public String toString() {
        return "TypeBuilder{sig=" + bufferToString() + '}';
    }
}
