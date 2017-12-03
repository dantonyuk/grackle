package com.github.hyla.grackle.query;

import com.github.hyla.grackle.predicate.Predicate;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QueryMethodExecutor {
    private final String propertyName;
    private final Predicate predicate;
    private final List<Alias> aliases;

    public <T, I extends Serializable, Q extends QueryImpl<T, I, Q>> QueryImpl<T, I, Q> apply(QueryImpl<T, I, Q> query, Object... args) {
        return query.copyWith(aliases, criteria -> predicate.applyPredicate(criteria, propertyName, args));
    }
}
