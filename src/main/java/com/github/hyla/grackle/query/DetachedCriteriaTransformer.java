package com.github.hyla.grackle.query;

import org.hibernate.criterion.DetachedCriteria;

@FunctionalInterface
public interface DetachedCriteriaTransformer {

    DetachedCriteria transform(DetachedCriteria criteria);
}
