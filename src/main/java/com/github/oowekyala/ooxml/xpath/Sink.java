package com.github.oowekyala.ooxml.xpath;

import java.util.stream.Stream;

import com.github.oowekyala.ooxml.xpath.MinXPath.Context;

class Sink<N> extends PathElement<N> {

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
