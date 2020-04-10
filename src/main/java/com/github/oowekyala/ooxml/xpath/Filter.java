package com.github.oowekyala.ooxml.xpath;

import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.github.oowekyala.ooxml.Nav;
import com.github.oowekyala.ooxml.xpath.MinXPath.Context;

/**
 *
 */
abstract class Filter<N> {

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
