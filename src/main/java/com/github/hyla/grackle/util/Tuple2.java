package com.github.hyla.grackle.util;

import lombok.Data;

import java.util.function.Function;

@Data(staticConstructor = "create")
public class Tuple2<A, B> {

    private final A _1;
    private final B _2;

    public <C, D> Tuple2<C, D> map(Function<A, C> mapper1, Function<B, D> mapper2) {
        return Tuple2.create(mapper1.apply(_1), mapper2.apply(_2));
    }

    public <C> Tuple2<C, B> map1(Function<A, C> mapper) {
        return Tuple2.create(mapper.apply(_1), _2);
    }

    public <C> Tuple2<A, C> map2(Function<B, C> mapper) {
        return Tuple2.create(_1, mapper.apply(_2));
    }
}
