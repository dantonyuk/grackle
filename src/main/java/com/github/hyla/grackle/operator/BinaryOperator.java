package com.github.hyla.grackle.operator;

import com.github.hyla.grackle.query.DetachedCriteriaTransformer;
import org.hibernate.criterion.DetachedCriteria;

@FunctionalInterface
public interface BinaryOperator<T> extends Operator {

    DetachedCriteria apply(DetachedCriteria criteria, String propertyName, T value);

    @Override
    @SuppressWarnings("unchecked")
    default DetachedCriteriaTransformer apply(String propertyName, Object... args) {
        assert args.length == 1 : "Binary operator can receive only one argument";
        return criteria -> apply(criteria, propertyName, (T) args[0]);
    }
}
