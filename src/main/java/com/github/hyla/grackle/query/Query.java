package com.github.hyla.grackle.query;

import java.util.List;
import java.util.Optional;

public interface Query<T> {

    Optional<T> uniqueResult();

    Optional<T> first();

    List<T> list();

    List<T> list(int limit);

    List<T> list(int first, int limit);

    long count();

    boolean exists();
}
