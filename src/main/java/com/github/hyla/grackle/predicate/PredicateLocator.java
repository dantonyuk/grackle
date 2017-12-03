package com.github.hyla.grackle.predicate;

import java.util.Map;
import java.util.Optional;

public interface PredicateLocator {

    Map<String, UnaryPredicate> getUnaryPredicates();
    Map<String, BinaryPredicate> getBinaryPredicates();
    Map<String, TernaryPredicate> getTernaryPredicates();

    default Optional<Predicate> lookup(String predicateName) {
        Predicate predicate = getUnaryPredicates().get(predicateName);
        if (predicate != null) return Optional.of(predicate);
        predicate = getBinaryPredicates().get(predicateName);
        if (predicate != null) return Optional.of(predicate);
        predicate = getTernaryPredicates().get(predicateName);
        return Optional.ofNullable(predicate);
    }
}
