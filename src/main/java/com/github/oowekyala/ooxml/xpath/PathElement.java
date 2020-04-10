package com.github.oowekyala.ooxml.xpath;

import java.util.stream.Stream;

import com.github.oowekyala.ooxml.xpath.MinXPath.Context;

/**
 *
 */
abstract class PathElement<N> {


    public abstract Stream<N> evaluate(Context<N> ctx);


    public abstract void acceptFilter(Filter<N> f);


    /**
     * Compose the given path element after this one. Implementations can override this with an optimised impl in
     * some cases.
     */
    public PathElement<N> andThen(PathElement<N> downstream) {
        if (downstream instanceof AxisStep.SelfStep) {
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
            Stream<N> up = upstream.evaluate(ctx);
            return downstream.evaluate(ctx.withUpstream(up));
        }


        @Override
        public String toString() {
            return upstream + "/" + downstream;
        }
    }
}
