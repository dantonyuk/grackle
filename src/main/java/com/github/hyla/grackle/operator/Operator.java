package com.github.hyla.grackle.operator;

import com.github.hyla.grackle.query.DetachedCriteriaTransformer;

public interface Operator {

    DetachedCriteriaTransformer apply(String propertyName, Object... args);
}
