package com.github.oowekyala.ooxml.xpath;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Node;

import com.github.oowekyala.ooxml.Nav;
import com.github.oowekyala.ooxml.xpath.MinXPath.Context;

abstract class AxisStep<N> extends PathElement<N> {

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


        ChildStep(short outputNodeType, String nameTest) {
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
