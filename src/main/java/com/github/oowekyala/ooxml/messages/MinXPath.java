package com.github.oowekyala.ooxml.messages;

import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.messages.Annots.Nullable;
import com.github.oowekyala.ooxml.messages.Annots.OneBased;
import com.github.oowekyala.ooxml.messages.MinXPath.AxisStep.AttrStep;
import com.github.oowekyala.ooxml.messages.MinXPath.AxisStep.ChildStep;

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
        return path.evaluate(StreamUtils.streamOf(start));
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
            path = path.andThen(new ChildStep(Node.ELEMENT_NODE, null));
            break;
        case '.':
            cur = b.consumeChar(cur, '.');
            path = path.andThen(SelfStep.INSTANCE);
            break;
        default:
            if (b.isIdentStart(cur)) {
                cur = identifier(cur, b, "ident");
                path = path.andThen(new ChildStep(Node.ELEMENT_NODE, b.popStr()));
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
        b.push(path);
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
        b.push(new Filter.Pos(Integer.parseInt(b.popStr())));
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
        return new Filter.Pred(n -> {
            if (!n.hasAttributes()) {
                return false;
            }
            Node attr = n.getAttributes().getNamedItem(name);
            if (attr == null) {
                return false;
            }
            return value.equals(attr.getNodeValue());
        }) {
            @Override
            public String toString() {
                return "[@" + name + " = '" + value + "']";
            }
        };
    }


    abstract static class PathElement {


        public abstract Stream<Node> evaluate(Stream<Node> upstream);


        /**
         * Compose the given path element after this one. Implementations can override this with an optimised impl in
         * some cases.
         */
        public PathElement andThen(PathElement downstream) {
            if (downstream instanceof SelfStep) {
                return this;
            } else if (downstream instanceof Sink) {
                return downstream;
            }
            return new PathElement() {
                @Override
                public Stream<Node> evaluate(Stream<Node> upstream) {
                    return downstream.evaluate(PathElement.this.evaluate(upstream));
                }


                @Override
                public String toString() {
                    return PathElement.this + "/" + downstream;
                }
            };
        }
    }

    static abstract class AxisStep extends PathElement {

        private final short kindTest;
        private final @Nullable String nameTest;
        private final List<Filter> predicates = new ArrayList<>();


        AxisStep(short kindTest, @Nullable String nameTest) {
            this.kindTest = kindTest;
            this.nameTest = nameTest;
        }


        @Override
        public PathElement andThen(PathElement downstream) {
            if (downstream instanceof Filter) {
                this.predicates.add((Filter) downstream);
                return this;
            }
            return super.andThen(downstream);
        }


        protected abstract Stream<Node> iterateAxis(Node node, short kindTest, @Nullable String nameTest);


        protected Stream<Node> defaultBaseFilter(Stream<Node> nodes, short kindTest, @Nullable String nameTest) {
            Stream<Node> result = nodes;
            result = result.filter(it -> it.getNodeType() == kindTest);
            if (nameTest != null) {
                result = result.filter(it -> nameTest.equals(it.getNodeName()));
            }
            return result;
        }


        private String nameTestToString() {
            return nameTest == null ? "" : nameTest;
        }


        protected abstract String axisName();


        @Override
        public String toString() {
            return axisName() + "::" + nodeTestToString() + filtersToString();
        }


        protected String nodeTestToString() {
            switch (kindTest) {
            case Node.ELEMENT_NODE:
                return "element-node(" + nameTestToString() + ")";
            case Node.ATTRIBUTE_NODE:
                return "attribute(" + nameTestToString() + ")";
            default:
                return "???(" + nameTestToString() + ")";
            }
        }


        protected String filtersToString() {
            return predicates.stream().map(it -> "[" + it + "]").collect(Collectors.joining());
        }


        @Override
        public Stream<Node> evaluate(Stream<Node> upstream) {
            return upstream.flatMap(n -> {
                Stream<Node> result = iterateAxis(n, kindTest, nameTest);
                result = result.filter(it -> it.getNodeType() == kindTest);
                if (nameTest != null) {
                    result = result.filter(it -> nameTest.equals(it.getNodeName()));
                }

                for (Filter pred : predicates) {
                    result = pred.evaluate(result);
                }
                return result;
            });
        }


        static class ChildStep extends AxisStep {


            private ChildStep(short outputNodeType, String nameTest) {
                super(outputNodeType, nameTest);
            }


            @Override
            protected Stream<Node> iterateAxis(Node node, short kindTest, @Nullable String nameTest) {
                Stream<Node> children = StreamUtils.streamOf(XmlErrorUtils.convertNodeList(node.getChildNodes()));
                return defaultBaseFilter(children, kindTest, nameTest);
            }


            @Override
            protected String axisName() {
                return "child";
            }
        }


        static class AttrStep extends AxisStep {

            AttrStep(String attrName) {
                super(Node.ATTRIBUTE_NODE, attrName);
            }


            @Override
            protected Stream<Node> iterateAxis(Node node, short kindTest, @Nullable String nameTest) {
                assert kindTest == Node.ATTRIBUTE_NODE;
                if (!node.hasAttributes()) {
                    return Stream.empty();
                }

                NamedNodeMap attributes = node.getAttributes();
                if (nameTest == null) {
                    return new AbstractList<Node>() {
                        @Override
                        public int size() {
                            return attributes.getLength();
                        }


                        @Override
                        public Node get(int index) {
                            return attributes.item(index);
                        }
                    }.stream();
                }


                Node attr = attributes.getNamedItem(nameTest);
                if (attr == null) {
                    return Stream.empty();
                }
                return StreamUtils.streamOf(attr);
            }


            @Override
            protected String axisName() {
                return "attribute";
            }
        }
    }

    static abstract class Filter extends PathElement {

        static class Pred extends Filter {

            final Predicate<? super Node> predicate;


            Pred(Predicate<? super Node> predicate) {this.predicate = predicate;}


            @Override
            public Stream<Node> evaluate(Stream<Node> upstream) {
                return upstream.filter(predicate);
            }
        }

        static class Pos extends Filter {

            final @OneBased int pos;


            Pos(int pos) {
                this.pos = pos;
            }


            @Override
            public String toString() {
                return "[" + pos + "]";
            }


            @Override
            public Stream<Node> evaluate(Stream<Node> upstream) {
                return upstream.skip(pos - 1).limit(1);
            }
        }
    }

    static class SelfStep extends PathElement {

        static final SelfStep INSTANCE = new SelfStep();


        private SelfStep() { }


        @Override
        public Stream<Node> evaluate(Stream<Node> upstream) {
            return upstream;
        }


        @Override
        public PathElement andThen(PathElement downstream) {
            return downstream;
        }


        @Override
        public String toString() {
            return "[ true() ]";
        }
    }

    static class Sink extends PathElement {

        static final Sink INSTANCE = new Sink();


        @Override
        public Stream<Node> evaluate(Stream<Node> upstream) {
            return Stream.empty();
        }


        @Override
        public PathElement andThen(PathElement downstream) {
            return this;
        }


        @Override
        public String toString() {
            return "[ false() ]";
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
