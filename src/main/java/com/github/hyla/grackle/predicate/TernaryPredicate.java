package com.github.hyla.grackle.predicate;

import org.hibernate.criterion.DetachedCriteria;

@FunctionalInterface
public interface TernaryPredicate<T> extends Predicate {

    DetachedCriteria apply(DetachedCriteria criteria, String propertyName, T left, T right);

    @Override
    @SuppressWarnings("unchecked")
    default DetachedCriteria applyPredicate(DetachedCriteria criteria, String propertyName, Object... args) {
        assert args.length == 2 : "Ternary predicate can receive exactly two arguments";
        return apply(criteria, propertyName, (T) args[0], (T) args[1]);
    }
}
