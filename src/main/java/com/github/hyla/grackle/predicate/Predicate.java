package com.github.hyla.grackle.predicate;

import org.hibernate.criterion.DetachedCriteria;

public interface Predicate {
    DetachedCriteria applyPredicate(DetachedCriteria criteria, String propertyName, Object... args);
}
