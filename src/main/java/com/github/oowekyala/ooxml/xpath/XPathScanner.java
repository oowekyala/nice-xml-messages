package com.github.oowekyala.ooxml.xpath;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.github.oowekyala.ooxml.internal_not_api.InternalUtil;

class XPathScanner<N> {

    private final Deque<String> stringStack = new ArrayDeque<>(1);
    final Deque<PathElement<N>> stack = new ArrayDeque<>(1);

    private final String text;
    private final List<Token> tokens;
    protected final int end;


    class Token {

        final TokenKinds kind;
        final int start;
        final int end;


        Token(TokenKinds kind, int start, int end) {
            this.kind = kind;
            this.start = start;
            this.end = end;
        }


        String image() {
            return text.substring(start, end);
        }
    }


    XPathScanner(String descriptor) {
        if (descriptor == null || descriptor.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot scan \"" + descriptor + "\"");
        }

        descriptor = descriptor.trim();

        XPathLexer lexer = new XPathLexer(new StringReader(descriptor));

        List<Token> tokens = new ArrayList<>();

        int off = 0;
        TokenKinds k;
        while (!lexer.yyatEOF()) {
            try {
                k = lexer.yylex();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            int start = off;
            off += lexer.yylength();
            if (k != TokenKinds.IGNORED) {
                tokens.add(new Token(k, start, off));
            }
        }

        this.text = descriptor;
        this.end = tokens.size();
        this.tokens = tokens;
    }


    public Token tokenAt(int off) {
        return tokens.get(off);
    }


    public boolean isAt(int off, TokenKinds k) {
        return tokens.get(off).kind == k;
    }


    public int consumeChar(int start, TokenKinds l, String s) {
        Token t = tokenAt(start);
        if (t.kind != l) {
            throw expected(s, start);
        }
        return start + 1;
    }


    public int consumeChar(int start, TokenKinds l) {
        return consumeChar(start, l, "" + l);
    }


    public RuntimeException expected(String expectedWhat, int pos) {
        String prefix = "Parse exception in:  ";
        String messageLine = InternalUtil.buildCaretLine(
            "Expected " + expectedWhat,
            prefix.length() + pos,
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
        consumeChar(e, TokenKinds.EOF, "end of input");
    }


    public String bufferToString() {
        return bufferToString(0, end);
    }


    public String bufferToString(int start, int end) {
        if (start == end) {
            return "";
        }
        return text.substring(start, end - start);
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


    void push(PathElement<N> p) {
        stack.push(p);
    }


    PathElement<N> pop() {
        return stack.removeFirst();
    }
}
