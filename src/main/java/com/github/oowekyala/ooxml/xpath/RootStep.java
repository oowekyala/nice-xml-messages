package com.github.oowekyala.ooxml.xpath;

import java.util.stream.Stream;

import com.github.oowekyala.ooxml.xpath.MinXPath.Context;

/**
 *
 */
class RootStep<N> extends PathElement<N> {

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
