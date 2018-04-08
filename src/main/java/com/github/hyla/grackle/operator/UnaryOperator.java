package com.github.hyla.grackle.operator;

import com.github.hyla.grackle.query.DetachedCriteriaTransformer;
import org.hibernate.criterion.DetachedCriteria;

@FunctionalInterface
public interface UnaryOperator extends Operator {

    DetachedCriteria apply(DetachedCriteria criteria, String propertyName);

    @Override
    default DetachedCriteriaTransformer apply(String propertyName, Object... args) {
        assert args == null || args.length == 0 : "Unary operator can receive no arguments";
        return criteria -> apply(criteria, propertyName);
    }
}
