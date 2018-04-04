package com.github.hyla.grackle.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtil {

    public StreamUtil() {
    }

    public static <T> Stream<T> whileDo(Supplier<Boolean> condition, Supplier<T> generator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                new Iterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return condition.get();
                    }

                    @Override
                    public T next() {
                        return generator.get();
                    }
                }, Spliterator.ORDERED), false);
    }
}
