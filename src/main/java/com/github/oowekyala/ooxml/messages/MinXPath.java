package com.github.oowekyala.ooxml.messages;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Annots.OneBased;

/**
 *
 */
public final class MinXPath {

    // /a/b[1]/c/@A


    static class XPathScanner extends SimpleScanner {

        private final Deque<PathElement> stack = new ArrayDeque<>(1);
        private final Deque<String> stringStack = new ArrayDeque<>(1);
        private final Deque<Predicate<Node>> filters = new ArrayDeque<>(1);

        XPathScanner(String descriptor) {
            super(descriptor);
        }


        void push(PathElement p) {
            stack.push(p);
        }


        PathElement pop() {
            return stack.removeFirst();
        }


        void pushStr(String p) {
            stringStack.push(p);
        }


        String popStr() {
            return stringStack.removeFirst();
        }


    }


    private static int parseStep(final int start, XPathScanner b) {
        int cur = start;
        if (isIdentifierChar(b.charAt(cur))) {
            StringBuilder nameTest = new StringBuilder();
            cur = identifier(cur, b, nameTest);


        }
    }


    private static int parsePredicate(final int start, XPathScanner b) {
        int cur = start;
        if (b.charAt(cur) == '[') {
            cur = b.consumeChar(cur, '[');
            cur = b.skipWhitespace(cur);
            if (b.isDigit(cur)) {
                cur = numberPredicate(cur, b);
            } else if (b.charAt(cur) == '@') {
                cur = attrEqualsPredicate(cur, b);
            } else {
                throw b.expected("attribute test, or number", cur);
            }
            cur = b.consumeChar(cur, ']');
        } else {
            b.push(FilterTest.ALWAYS);
        }

        return cur;
    }


    static int numberPredicate(final int start, XPathScanner b) {
        int cur = number(start, b);
        int num = Integer.parseInt(b.popStr());

    }


    static int attrEqualsPredicate(final int start, XPathScanner b) {
        int cur = b.consumeChar(start, '@', "attribute");
        cur = identifier(cur, b, "attribute name");

        String attrName = b.popStr();

        cur = b.skipWhitespace(cur);
        cur = b.consumeChar(cur, '=', "attribute comparison");
        cur = b.skipWhitespace(cur);
        if (b.isDigit(cur)) {
            cur = number(cur, b);
        } else if (b.charAt(cur) == '\'') {
            cur = string(cur, b);
        } else {
            throw b.expected("string or number", cur);
        }
        b.push(FilterTest.attrTest(attrName, b.popStr()));
        return cur;
    }


    static int number(final int start, XPathScanner b) {
        int cur = start;
        if (!b.isDigit(cur)) {
            throw b.expected("number", cur);
        }
        do {
            cur++;
        } while (b.isDigit(cur) && b.isInRange(cur));
        b.pushStr(b.bufferToString(start, cur));
        return cur;
    }


    static int string(final int start, XPathScanner b) {
        int cur = b.consumeChar(start, '\'', "string delimiter (single quote)");
        do {
            cur++;
        } while (b.charAt(cur) != '\'' && b.isInRange(cur));
        if (!b.isInRange(cur)) {
            throw b.expected("closing string delimiter (single quote)", cur);
        }
        b.pushStr(b.bufferToString(start + 1, cur - 1));
        return cur;
    }


    static int identifier(final int start, XPathScanner b, String name) {
        int cur = start;
        if (!b.isIdentStart(cur)) {
            throw b.expected(name, cur);
        }
        do {
            cur++;
        } while (b.isIdentChar(cur) && b.isInRange(cur));
        b.pushStr(b.bufferToString(start, cur));
        return cur;
    }


    private static boolean isIdentifierChar(char c) {
        switch (c) {
        case '.':
        case ';':
        case ':':
        case '[':
        case '/':
        case '<':
        case '>':
            return false;
        default:
            return true;
        }
    }


    interface PathElement {

        Stream<Node> evaluate(Stream<Node> context);

    }


    static class ChildStep implements PathElement {

        private final short outputNodeType;
        private final @OneBased int positionFilter;


        ChildStep(short outputNodeType,
                  @OneBased int position) {
            this.outputNodeType = outputNodeType;
            this.positionFilter = position;
        }


        @Override
        public Stream<Node> evaluate(Stream<Node> context) {
            if (positionFilter > 0) {
                return context.flatMap(it -> {
                    List<Node> children = XmlErrorUtils.convertNodeList(it.getChildNodes());
                    if (children.size() < positionFilter) {
                        int seen = 0;
                        for (Node c : children) {
                            if (hasExpectedType(c)) {
                                seen++;
                            }
                            if (seen == positionFilter) {
                                return Stream.of(c);
                            }
                        }
                    }
                    return Stream.empty();
                });
            }

            return context.flatMap(
                it -> XmlErrorUtils.convertNodeList(it.getChildNodes())
                                   .stream()
                                   .filter(this::hasExpectedType)
            );
        }


        private boolean hasExpectedType(Node o) {
            return o.getNodeType() == outputNodeType;
        }
    }


    @OneBased
    static int indexInParent(Node n) {
        Node p = n.getParentNode();
        if (p == null) {
            return 1;
        }
        return 1 + XmlErrorUtils.convertNodeList(p.getChildNodes()).indexOf(n);
    }


    static class FilterTest implements PathElement {

        static final FilterTest ALWAYS = new FilterTest(n -> true);

        private final Predicate<? super Node> filter;


        FilterTest(Predicate<? super Node> filter) {
            this.filter = filter;
        }


        public static FilterTest nameTest(String name) {
            return new FilterTest(n -> name.equals(n.getNodeName()));
        }


        public static FilterTest posTest(@OneBased int index) {
            return new FilterTest(n -> indexInParent(n) == index);
        }


        public static FilterTest attrTest(String name, String value) {
            return new FilterTest(n -> {
                if (!n.hasAttributes()) {
                    return false;
                }
                Node attr = n.getAttributes().getNamedItem(name);
                if (attr == null) {
                    return false;
                }
                return value.equals(attr.getNodeValue());
            });
        }


        @Override
        public Stream<Node> evaluate(Stream<Node> context) {
            return context.filter(filter);
        }
    }

}
