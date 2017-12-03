package com.github.hyla.grackle.predicate;

import org.hibernate.criterion.DetachedCriteria;

@FunctionalInterface
public interface BinaryPredicate<T> extends Predicate {

    DetachedCriteria apply(DetachedCriteria criteria, String propertyName, T value);

    @Override
    @SuppressWarnings("unchecked")
    default DetachedCriteria applyPredicate(DetachedCriteria criteria, String propertyName, Object... args) {
        assert args.length == 1 : "Binary predicate can receive only one argument";
        return apply(criteria, propertyName, (T) args[0]);
    }
}
