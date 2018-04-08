package com.github.hyla.grackle.operator;

import org.hibernate.criterion.DetachedCriteria;

@FunctionalInterface
public interface UnaryOperator extends Operator {

    DetachedCriteria apply(DetachedCriteria criteria, String propertyName);

    @Override
    default DetachedCriteria apply(DetachedCriteria criteria, String propertyName, Object... args) {
        assert args == null || args.length == 0 : "Unary operator can receive no arguments";
        return apply(criteria, propertyName);
    }
}
