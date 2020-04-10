package com.github.oowekyala.ooxml.messages;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Annots.OneBased;

/**
 * Minimal XPath engine. Not optimised or anything, just a
 * convenient way to retrieve nodes (esp in tests). For
 * example:
 * <pre>{@code
 *
 * List<Node> nodes = MinXPath.parse(expr).evaluate(root).collect(Collectors.toList());
 *
 * }</pre>
 *
 * <p>Limitations:
 * <ul>
 * <li>Path expressions must be relative. They cannot start with '/' or '//'
 * <li>Only child, attribute, and self axes are supported, and
 * only with their shorthand form. This also means that descendant
 * steps ('//') are unsupported.
 * <li>No arithmetic or boolean expressions, no functions, no comments
 * <li>No namespaced names, everything is treated as a local name
 * <li>Strings must be single quoted, there is no support for either
 * delimiter escape or double quoted strings.
 * </ul>
 *
 * <p>So what is supported?
 * <pre>{@code
 *
 * a/b/c
 * a/b[1]/c             (: select a child with a 1-based index :)
 * a/@attr              (: a path can contain an attribute (should really only end with it) :)
 * a/*[@size = 1]       (: wildcard name test, number literals for attribute tests :)
 * a/.[@size = 1]       (: . is shorthand for the self axis :)
 * e/*[@a = 1][@b = 2]  (: multiple predicates mimic AND boolan expressions :)
 *
 * }</pre>
 *
 *
 */
public final class MinXPath {

    private final PathElement path;


    public MinXPath(PathElement path) {
        this.path = path;
    }


    /**
     * Evaluate this expression on the given node as the start
     * element. Returns a stream of nodes that match the full path.
     *
     * @param start Start context node
     * @return A stream of nodes
     */
    public Stream<Node> evaluate(Node start) {
        return path.evaluate(Stream.of(start));
    }


    /**
     * Parse a string expression into a runnable form.
     *
     * @param expression Expression
     * @throws IllegalArgumentException If the string is empty or null, or whitespace
     * @throws IllegalArgumentException If parsing fails
     */
    public static MinXPath parse(String expression) {
        XPathScanner scanner = new XPathScanner(expression);
        int end = path(scanner.start, scanner);
        scanner.expectEoI(end);
        return new MinXPath(scanner.pop());
    }


    private static int path(final int start, XPathScanner b) {

        b.push(SelfStep.INSTANCE);
        int cur = b.skipWhitespace(start);
        cur = step(cur, b);
        assert b.stack.size() == 1;
        cur = b.skipWhitespace(cur);

        while (b.charAt(cur) == '/') {
            cur = b.consumeChar(cur, '/');
            assert b.stack.size() == 1 : b.stack;
            cur = step(cur, b);
            assert b.stack.size() == 1 : b.stack;
            cur = b.skipWhitespace(cur);
        }

        return cur;
    }


    private static int step(final int start, XPathScanner b) {
        int cur = start;
        PathElement path = b.pop();

        switch (b.charAt(cur)) {
        case '@':
            cur = attr(cur, b);
            String attrName = b.popStr();
            path = path.andThen(new AttrStep(attrName));
            break;
        case '*':
            cur = b.consumeChar(cur, '*');
            path = path.andThen(new ChildStep(Node.ELEMENT_NODE));
            break;
        case '.':
            cur = b.consumeChar(cur, '.');
            path = path.andThen(SelfStep.INSTANCE);
            break;
        default:
            if (b.isIdentStart(cur)) {
                cur = identifier(cur, b, "ident");
                path = path.andThen(new ChildStep(Node.ELEMENT_NODE));
                path = path.andThen(nameTest(b.popStr()));
            } else {
                throw b.expected("step (*, ., name, or @attr)", cur);
            }
            break;
        }

        cur = b.skipWhitespace(cur);

        while (b.charAt(cur) == '[') {
            cur = predicate(cur, b);
            cur = b.skipWhitespace(cur);
            path = path.andThen(b.pop());
        }
        return cur;
    }


    private static int predicate(final int start, XPathScanner b) {
        int cur = b.consumeChar(start, '[', "predicate");
        cur = b.skipWhitespace(cur);
        if (b.isDigit(cur)) {
            cur = numberPredicate(cur, b);
        } else if (b.charAt(cur) == '@') {
            cur = attrEqualsPredicate(cur, b);
        } else {
            throw b.expected("attribute test, or number", cur);
        }
        cur = b.consumeChar(cur, ']');
        return cur;
    }


    static int numberPredicate(final int start, XPathScanner b) {
        int cur = number(start, b);
        b.push(new PosTest(Integer.parseInt(b.popStr())));
        return cur;
    }


    static int attrEqualsPredicate(final int start, XPathScanner b) {
        int cur = attr(start, b);
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
        String attrValue = b.popStr();
        b.push(attrTest(attrName, attrValue));
        return cur;
    }


    private static int attr(int start, XPathScanner b) {
        int cur = b.consumeChar(start, '@', "attribute");
        return identifier(cur, b, "attribute name");
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
        cur = b.consumeChar(cur, '\'', "closing string delimiter (single quote)");
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


    private static PathElement attrTest(String name, String value) {
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


    public static PathElement nameTest(String name) {
        return new NameTest(name);
    }


    interface PathElement {

        Stream<Node> evaluate(Stream<Node> context);


        /**
         * Compose the given path element after this one. Implementations can override this with an optimised impl in
         * some cases.
         */
        default PathElement andThen(PathElement p) {
            return nodes -> p.evaluate(this.evaluate(nodes));
        }
    }

    static class SelfStep implements PathElement {

        static final SelfStep INSTANCE = new SelfStep();


        private SelfStep() {

        }


        @Override
        public Stream<Node> evaluate(Stream<Node> context) {
            return context;
        }


        @Override
        public PathElement andThen(PathElement p) {
            return p;
        }
    }

    static class AttrStep implements PathElement {

        private final String attrName;


        AttrStep(String attrName) {
            this.attrName = attrName;
        }


        @Override
        public Stream<Node> evaluate(Stream<Node> context) {
            return context.flatMap(it -> {
                if (!it.hasAttributes()) {
                    return Stream.empty();
                }
                Node attr = it.getAttributes().getNamedItem(attrName);
                if (attr == null) {
                    return Stream.empty();
                }
                return Stream.of(attr);
            });
        }
    }


    static class ChildStep implements PathElement {

        private final short outputNodeType;
        private final @OneBased int positionFilter;


        private ChildStep(short outputNodeType,
                          @OneBased int position) {
            this.outputNodeType = outputNodeType;
            this.positionFilter = position;
        }


        ChildStep(short outputNodeType) {
            this(outputNodeType, 0);
        }


        @Override
        public Stream<Node> evaluate(Stream<Node> context) {
            if (positionFilter > 0) {
                return context.flatMap(it -> {
                    List<Node> children = XmlErrorUtils.convertNodeList(it.getChildNodes());
                    if (children.size() < positionFilter) {
                        int seen = 0;
                        for (Node c : children) {
                            if (c.getNodeType() == outputNodeType) {
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
                                   .filter(o -> o.getNodeType() == outputNodeType)
            );
        }


        @Override
        public PathElement andThen(PathElement p) {
            if (p instanceof PosTest) {
                @OneBased int pos = ((PosTest) p).position;
                if (this.positionFilter == 0) {
                    return new ChildStep(outputNodeType, pos);
                } else if (pos == this.positionFilter) {
                    return this;
                } else {
                    return Sink.INSTANCE;
                }
            }
            return PathElement.super.andThen(p);
        }
    }


    static @OneBased int indexInParent(Node n) {
        Node p = n.getParentNode();
        if (p == null) {
            return 1;
        }
        return 1 + XmlErrorUtils.convertNodeList(p.getChildNodes()).indexOf(n);
    }


    static class Sink implements PathElement {

        static final Sink INSTANCE = new Sink();


        @Override
        public Stream<Node> evaluate(Stream<Node> context) {
            return Stream.empty();
        }


        @Override
        public PathElement andThen(PathElement p) {
            return this;
        }
    }

    static class PosTest implements PathElement {

        private final @OneBased int position;


        PosTest(@OneBased int position) {
            this.position = position;
        }


        @Override
        public Stream<Node> evaluate(Stream<Node> context) {
            return context.filter(n -> indexInParent(n) == position);
        }
    }

    static class NameTest implements PathElement {

        private final @OneBased String name;


        NameTest(String name) {
            this.name = name;
        }


        @Override
        public Stream<Node> evaluate(Stream<Node> context) {
            return context.filter(n -> name.equals(n.getNodeName()));
        }
    }


    static class FilterTest implements PathElement {

        private final Predicate<? super Node> filter;

        FilterTest(Predicate<? super Node> filter) {
            this.filter = filter;
        }

        @Override
        public Stream<Node> evaluate(Stream<Node> context) {
            return context.filter(filter);
        }
    }


    static class XPathScanner extends SimpleScanner {

        private final Deque<PathElement> stack = new ArrayDeque<>(1);
        private final Deque<String> stringStack = new ArrayDeque<>(1);


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


}
