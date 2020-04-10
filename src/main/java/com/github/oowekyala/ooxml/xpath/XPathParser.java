package com.github.oowekyala.ooxml.xpath;

import static com.github.oowekyala.ooxml.xpath.TokenKinds.AT;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.IDENTIFIER;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.LBRACKET;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.LIT_DECIMAL;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.LIT_DOUBLE;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.LIT_INTEGER;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.LIT_STRING;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.OP;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.PATH;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.PERIOD;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.RBRACKET;
import static com.github.oowekyala.ooxml.xpath.TokenKinds.STAR;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.xpath.AxisStep.AttrStep;
import com.github.oowekyala.ooxml.xpath.AxisStep.ChildStep;
import com.github.oowekyala.ooxml.xpath.AxisStep.SelfStep;
import com.github.oowekyala.ooxml.xpath.Filter.PositionFilter;
import com.github.oowekyala.ooxml.xpath.Filter.Predicate;
import com.github.oowekyala.ooxml.xpath.XPathScanner.Token;


class XPathParser {


    static <N> int path(final int start, XPathScanner<N> b) {

        b.push(RootStep.INSTANCE);
        int cur = step(start, b);
        assert b.stack.size() == 1;

        while (b.isAt(cur, PATH)) {
            cur = b.consumeChar(cur, PATH);
            assert b.stack.size() == 1 : b.stack;
            cur = step(cur, b);
            assert b.stack.size() == 1 : b.stack;
        }

        return cur;
    }


    private static <N> int step(final int start, XPathScanner<N> b) {
        int cur = start;
        PathElement<N> path = b.pop();

        Token t = b.tokenAt(cur);
        switch (t.kind) {
        case AT:
            cur = attr(cur, b);
            String attrName = b.popStr();
            path = path.andThen(new AttrStep<>(attrName));
            break;
        case STAR:
            cur = b.consumeChar(cur, STAR);
            path = path.andThen(new ChildStep<>(Node.ELEMENT_NODE, null));
            break;
        case PERIOD:
            cur = b.consumeChar(cur, PERIOD);
            path = path.andThen(new SelfStep<>((short) 0, null));
            break;
        case IDENTIFIER:
            cur = b.consumeChar(cur, IDENTIFIER);
            path = path.andThen(new ChildStep<>(Node.ELEMENT_NODE, t.image()));
            break;
        default:
            throw b.expected("step (*, ., name, or @attr)", cur);
        }

        while (b.tokenAt(cur).kind == LBRACKET) {
            cur = predicate(cur, b, path);
        }
        b.push(path);
        return cur;
    }


    private static <N> int predicate(final int start, XPathScanner<N> b, PathElement<N> owner) {
        int cur = b.consumeChar(start, LBRACKET, "predicate");
        switch (b.tokenAt(cur).kind) {
        case LIT_INTEGER:
            cur = numberPredicate(cur, b, owner);
            break;
        case AT:
            cur = attrEqualsPredicate(cur, b, owner);
            break;
        default:
            throw b.expected("attribute test, or integer", cur);
        }
        cur = b.consumeChar(cur, RBRACKET);
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
        cur = b.consumeChar(cur, OP, "operator");
        if (b.isAt(cur, LIT_INTEGER)
            || b.isAt(cur, LIT_DECIMAL)
            || b.isAt(cur, LIT_DOUBLE)) {

            cur = number(cur, b);

        } else if (b.isAt(cur, LIT_STRING)) {
            cur = string(cur, b);
        } else {
            throw b.expected("string or number", cur);
        }
        String attrValue = b.popStr();
        owner.acceptFilter(attrTest(attrName, attrValue));
        return cur;
    }


    private static <N> int attr(int start, XPathScanner<N> b) {
        int cur = b.consumeChar(start, AT, "attribute");
        return identifier(cur, b, "attribute name");
    }


    static <N> int number(final int start, XPathScanner<N> b) {
        Token t = b.tokenAt(start);
        switch (t.kind) {
        case LIT_INTEGER:
        case LIT_DECIMAL:
        case LIT_DOUBLE:
            b.pushStr(t.image());
            return start + 1;
        default:
            throw b.expected("number", start);
        }
    }


    static <N> int string(final int start, XPathScanner<N> b) {
        int cur = b.consumeChar(start, LIT_STRING);
        b.pushStr(b.tokenAt(start).image());
        return cur;
    }


    static <N> int identifier(final int start, XPathScanner<N> b, String name) {
        int cur = b.consumeChar(start, IDENTIFIER, name);
        b.pushStr(b.tokenAt(start).image());
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
