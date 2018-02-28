package com.github.hyla.grackle.operator;

import org.hibernate.criterion.DetachedCriteria;

@FunctionalInterface
public interface TernaryOperator<T> extends Operator {

    DetachedCriteria apply(DetachedCriteria criteria, String propertyName, T left, T right);

    @Override
    @SuppressWarnings("unchecked")
    default DetachedCriteria apply(DetachedCriteria criteria, String propertyName, Object... args) {
        assert args.length == 2 : "Ternary operator can receive exactly two arguments";
        return apply(criteria, propertyName, (T) args[0], (T) args[1]);
    }
}
