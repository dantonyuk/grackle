package com.github.hyla.grackle.predicate;

import org.hibernate.criterion.DetachedCriteria;

@FunctionalInterface
public interface UnaryPredicate extends Predicate {

    DetachedCriteria apply(DetachedCriteria criteria, String propertyName);

    @Override
    default DetachedCriteria applyPredicate(DetachedCriteria criteria, String propertyName, Object... args) {
        assert args.length == 0 : "Unary predicate can receive no arguments";
        return apply(criteria, propertyName);
    }
}
