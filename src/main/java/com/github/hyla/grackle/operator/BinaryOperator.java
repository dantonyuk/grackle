package com.github.hyla.grackle.operator;

import org.hibernate.criterion.DetachedCriteria;

@FunctionalInterface
public interface BinaryOperator<T> extends Operator {

    DetachedCriteria apply(DetachedCriteria criteria, String propertyName, T value);

    @Override
    @SuppressWarnings("unchecked")
    default DetachedCriteria apply(DetachedCriteria criteria, String propertyName, Object... args) {
        assert args.length == 1 : "Binary operator can receive only one argument";
        return apply(criteria, propertyName, (T) args[0]);
    }
}
