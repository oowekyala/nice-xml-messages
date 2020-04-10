package com.github.oowekyala.ooxml.xpath;

import java.util.ArrayDeque;
import java.util.Deque;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.xpath.AxisStep.AttrStep;
import com.github.oowekyala.ooxml.xpath.AxisStep.ChildStep;
import com.github.oowekyala.ooxml.xpath.AxisStep.SelfStep;
import com.github.oowekyala.ooxml.xpath.Filter.PositionFilter;
import com.github.oowekyala.ooxml.xpath.Filter.Predicate;


class XPathParser {


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


    static <N> int path(final int start, XPathScanner<N> b) {

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
}
