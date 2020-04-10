package com.github.oowekyala.ooxml;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.MinXPath.AxisStep.AttrStep;
import com.github.oowekyala.ooxml.MinXPath.AxisStep.ChildStep;
import com.github.oowekyala.ooxml.MinXPath.AxisStep.SelfStep;
import com.github.oowekyala.ooxml.MinXPath.Context.ContextImpl;
import com.github.oowekyala.ooxml.MinXPath.Filter.PositionFilter;
import com.github.oowekyala.ooxml.MinXPath.Filter.Predicate;

/**
 * Minimal XPath engine. Not optimised or anything, just a
 * convenient way to retrieve nodes (especially for tests).
 * For example:
 * <pre>{@code
 *
 * List<Node> nodes = MinXPath.parse(expr).evaluate(root).collect(Collectors.toList());
 *
 * }</pre>
 *
 *
 * <p>What is supported:
 * <pre>{@code
 *
 * a/b/c
 * a/b[1]/c             (: select a child with a (1-based) index :)
 * a/@attr              (: a path can contain an attribute (should really only end with it) :)
 * a/*[@size = 1]       (: wildcard name test, number literals for attribute tests :)
 * a/.[@size = 1]       (: . is shorthand for the self axis :)
 * e/*[@a = 1][@b = 2]  (: multiple predicates mimic AND boolan expressions :)
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
 */
public final class MinXPath<N> {

    private final PathElement<N> path;
    private final Nav<N> nav;


    public MinXPath(PathElement<N> path, Nav<N> nav) {
        this.path = path;
        this.nav = nav;
    }


    /**
     * Evaluate this expression on the given node as the start element. Returns a stream of nodes that match the full
     * path.
     *
     * @param start Start context node
     * @return A stream of nodes
     */
    public Stream<N> evaluate(N start) {
        ContextImpl<N> ctx = new ContextImpl<>(nav, StreamUtils.streamOf(start));
        return path.evaluate(ctx);
    }


    /**
     * Parse a string expression into a runnable form.
     *
     * @param expression Expression
     * @throws IllegalArgumentException If the string is empty or null, or whitespace
     * @throws IllegalArgumentException If parsing fails
     */
    public static MinXPath<Node> parse(String expression) {
        return parse(expression, Nav.W3C_DOM);
    }


    public static <N> MinXPath<N> parse(String expression, Nav<N> nav) {
        XPathScanner<N> scanner = new XPathScanner<>(expression);
        int end = path(scanner.start, scanner);
        scanner.expectEoI(end);
        return new MinXPath<>(scanner.pop(), nav);
    }


    private static <N> int path(final int start, XPathScanner<N> b) {

        b.push(RootStep.INSTANCE);
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


    private static <N> int step(final int start, XPathScanner<N> b) {
        int cur = start;
        PathElement<N> path = b.pop();

        switch (b.charAt(cur)) {
        case '@':
            cur = attr(cur, b);
            String attrName = b.popStr();
            path = path.andThen(new AttrStep<>(attrName));
            break;
        case '*':
            cur = b.consumeChar(cur, '*');
            path = path.andThen(new ChildStep<>(Node.ELEMENT_NODE, null));
            break;
        case '.':
            cur = b.consumeChar(cur, '.');
            path = path.andThen(new SelfStep<>((short) 0, null));
            break;
        default:
            if (b.isIdentStart(cur)) {
                cur = identifier(cur, b, "ident");
                path = path.andThen(new ChildStep<>(Node.ELEMENT_NODE, b.popStr()));
            } else {
                throw b.expected("step (*, ., name, or @attr)", cur);
            }
            break;
        }

        cur = b.skipWhitespace(cur);

        while (b.charAt(cur) == '[') {
            cur = predicate(cur, b, path);
            cur = b.skipWhitespace(cur);
        }
        b.push(path);
        return cur;
    }


    private static <N> int predicate(final int start, XPathScanner<N> b, PathElement<N> owner) {
        int cur = b.consumeChar(start, '[', "predicate");
        cur = b.skipWhitespace(cur);
        if (b.isDigit(cur)) {
            cur = numberPredicate(cur, b, owner);
        } else if (b.charAt(cur) == '@') {
            cur = attrEqualsPredicate(cur, b, owner);
        } else {
            throw b.expected("attribute test, or number", cur);
        }
        cur = b.consumeChar(cur, ']');
        return cur;
    }


    static <N> int numberPredicate(final int start, XPathScanner<N> b, PathElement<N> owner) {
        int cur = number(start, b);
        owner.acceptFilter(new PositionFilter<>(Integer.parseInt(b.popStr())));
        return cur;
    }


    static <N> int attrEqualsPredicate(final int start, XPathScanner<N> b, PathElement<N> owner) {
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
        owner.acceptFilter(attrTest(attrName, attrValue));
        return cur;
    }


    private static int attr(int start, SimpleScanner b) {
        int cur = b.consumeChar(start, '@', "attribute");
        return identifier(cur, b, "attribute name");
    }


    static int number(final int start, SimpleScanner b) {
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


    static int string(final int start, SimpleScanner b) {
        int cur = b.consumeChar(start, '\'', "string delimiter (single quote)");
        do {
            cur++;
        } while (b.charAt(cur) != '\'' && b.isInRange(cur));
        cur = b.consumeChar(cur, '\'', "closing string delimiter (single quote)");
        b.pushStr(b.bufferToString(start + 1, cur - 1));
        return cur;
    }


    static int identifier(final int start, SimpleScanner b, String name) {
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


    private static <N> Filter<N> attrTest(String name, String value) {
        return new Predicate<N>((n, nav) -> value.equals(nav.attrValue(n, name))) {
            @Override
            public String toString() {
                return "[@" + name + " = '" + value + "']";
            }
        };
    }


    interface Context<N> {

        Nav<N> nav();


        Stream<N> upstream();


        Context<N> withUpstream(Stream<N> n);


        class ContextImpl<N> implements Context<N> {

            private final Nav<N> nav;
            private final Stream<N> upstream;


            public ContextImpl(Nav<N> nav, Stream<N> upstream) {
                this.nav = nav;
                this.upstream = upstream;
            }


            @Override
            public Nav<N> nav() {
                return nav;
            }


            @Override
            public Stream<N> upstream() {
                return upstream;
            }


            @Override
            public Context<N> withUpstream(Stream<N> n) {
                return new ContextImpl<>(nav, n);
            }
        }
    }


    abstract static class PathElement<N> {


        public abstract Stream<N> evaluate(Context<N> ctx);


        public abstract void acceptFilter(Filter<N> f);


        /**
         * Compose the given path element after this one. Implementations can override this with an optimised impl in
         * some cases.
         */
        public PathElement<N> andThen(PathElement<N> downstream) {
            if (downstream instanceof SelfStep) {
                return this;
            } else if (downstream instanceof Sink) {
                return downstream;
            }

            return new PipedElement<>(this, downstream);
        }


        private static class PipedElement<N> extends PathElement<N> {

            private final PathElement<N> downstream;
            private final PathElement<N> upstream;


            public PipedElement(PathElement<N> upstream, PathElement<N> downstream) {
                this.upstream = upstream;
                this.downstream = downstream;
            }


            @Override
            public void acceptFilter(Filter<N> f) {
                downstream.acceptFilter(f);
            }


            @Override
            public Stream<N> evaluate(Context<N> ctx) {
                Stream<N> upstream = upstream.evaluate(ctx);
                return downstream.evaluate(ctx.withUpstream(upstream));
            }


            @Override
            public String toString() {
                return upstream + "/" + downstream;
            }
        }
    }

    static abstract class AxisStep<N> extends PathElement<N> {

        private final short kindTest;
        private final String nameTest;
        private final List<Filter<N>> predicates = new ArrayList<>();


        AxisStep(short kindTest, String nameTest) {
            this.kindTest = kindTest;
            this.nameTest = nameTest;
        }


        @Override
        public PathElement<N> andThen(PathElement<N> downstream) {
            return super.andThen(downstream);
        }


        public void acceptFilter(Filter<N> f) {
            this.predicates.add(f);
        }


        protected abstract Stream<N> iterateAxis(N node, Nav<N> nav, short kindTest, String nameTest);


        protected Stream<N> defaultBaseFilter(Stream<N> nodes, Nav<N> nav, short kindTest, String nameTest) {
            Stream<N> result = nodes;
            if (kindTest != 0) {
                result = result.filter(it -> nav.kind(it) == kindTest);
            }
            if (nameTest != null) {
                result = result.filter(it -> nameTest.equals(nav.name(it)));
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
            case 0:
                return "node(" + nameTestToString() + ")";
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
        public Stream<N> evaluate(Context<N> ctx) {
            return ctx.upstream()
                      .flatMap(n -> {
                          Nav<N> nav = ctx.nav();
                          Stream<N> result = iterateAxis(n, nav, kindTest, nameTest);
                          for (Filter<N> pred : predicates) {
                              result = pred.filter(ctx.withUpstream(result));
                          }
                          return result;
                      });
        }


        static class ChildStep<N> extends AxisStep<N> {


            private ChildStep(short outputNodeType, String nameTest) {
                super(outputNodeType, nameTest);
            }


            @Override
            protected Stream<N> iterateAxis(N node, Nav<N> nav, short kindTest, String nameTest) {
                Stream<N> children = StreamUtils.streamOf(nav.children(node));
                return defaultBaseFilter(children, nav, kindTest, nameTest);
            }


            @Override
            protected String axisName() {
                return "child";
            }
        }


        static class AttrStep<N> extends AxisStep<N> {

            AttrStep(String attrName) {
                super(Node.ATTRIBUTE_NODE, attrName);
            }


            @Override
            protected Stream<N> iterateAxis(N node, Nav<N> nav, short kindTest, String nameTest) {
                assert kindTest == Node.ATTRIBUTE_NODE;

                if (nav.kind(node) != Node.ELEMENT_NODE) {
                    return Stream.empty();
                }

                if (nameTest == null) {
                    return nav.attributes(node).stream();
                } else {
                    return StreamUtils.streamOfNullable(nav.attribute(node, nameTest));
                }
            }


            @Override
            protected String axisName() {
                return "attribute";
            }
        }

        static class SelfStep<N> extends AxisStep<N> {


            SelfStep(short kindTest, String nameTest) {
                super(kindTest, nameTest);
            }


            @Override
            protected Stream<N> iterateAxis(N node, Nav<N> nav, short kindTest, String nameTest) {
                return defaultBaseFilter(Stream.of(node), nav, kindTest, nameTest);
            }


            @Override
            protected String axisName() {
                return "self";
            }
        }
    }

    static abstract class Filter<N> {

        abstract Stream<N> filter(Context<N> ctx);


        static class Predicate<N> extends Filter<N> {

            final BiPredicate<? super N, Nav<N>> predicate;


            Predicate(BiPredicate<? super N, Nav<N>> predicate) {
                this.predicate = predicate;
            }


            @Override
            public Stream<N> filter(Context<N> ctx) {
                return ctx.upstream().filter(n -> predicate.test(n, ctx.nav()));
            }
        }

        static class PositionFilter<N> extends Filter<N> {

            final int pos;


            PositionFilter(int pos) {
                this.pos = pos;
            }


            @Override
            public String toString() {
                return "[" + pos + "]";
            }


            @Override
            public Stream<N> filter(Context<N> ctx) {
                return ctx.upstream().skip(pos - 1).limit(1);
            }
        }
    }


    static class RootStep<N> extends PathElement<N> {

        static final RootStep INSTANCE = new RootStep();


        private RootStep() { }


        @Override
        public Stream<N> evaluate(Context<N> ctx) {
            return ctx.upstream();
        }


        @Override
        public void acceptFilter(Filter<N> f) {
            throw new IllegalStateException("Root step should not receive filters, by construction of the parser");
        }


        @Override
        public PathElement<N> andThen(PathElement<N> downstream) {
            return downstream;
        }


        @Override
        public String toString() {
            return "[ true() ]";
        }
    }

    static class Sink<N> extends PathElement<N> {

        static final Sink INSTANCE = new Sink();


        private Sink() {}


        @Override
        public Stream<N> evaluate(Context<N> ctx) {
            return Stream.empty();
        }


        @Override
        public void acceptFilter(Filter<N> f) {
            // nothing to do
        }


        @Override
        public PathElement<N> andThen(PathElement<N> downstream) {
            return this;
        }


        @Override
        public String toString() {
            return "[ false() ]";
        }
    }

    static class XPathScanner<N> extends SimpleScanner {

        private final Deque<PathElement<N>> stack = new ArrayDeque<>(1);

        XPathScanner(String descriptor) {
            super(descriptor);
        }


        void push(PathElement<N> p) {
            stack.push(p);
        }


        PathElement<N> pop() {
            return stack.removeFirst();
        }
    }
}
