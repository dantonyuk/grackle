package com.github.hyla.grackle.operator;

import org.hibernate.criterion.DetachedCriteria;

public interface Operator {

    DetachedCriteria apply(DetachedCriteria criteria, String propertyName, Object... args);
}
