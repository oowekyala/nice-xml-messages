package com.github.oowekyala.ooxml.xpath;

import java.util.stream.Stream;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.Nav;
import com.github.oowekyala.ooxml.xpath.MinXPath.Context.ContextImpl;

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
        int end = XPathParser.path(0, scanner);
        scanner.expectEoI(end);
        return new MinXPath<>(scanner.pop(), nav);
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

}
