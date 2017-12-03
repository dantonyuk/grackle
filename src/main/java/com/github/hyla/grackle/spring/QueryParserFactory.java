package com.github.hyla.grackle.spring;

import com.github.hyla.grackle.predicate.PredicateLocator;
import com.github.hyla.grackle.query.EntityQuery;
import com.github.hyla.grackle.query.QueryParser;
import org.springframework.beans.factory.annotation.Autowired;

public class QueryParserFactory {

    @Autowired
    private PredicateLocator predicateLocator;

    public QueryParser newParser(Class<? extends EntityQuery> queryClass) {
        return new QueryParser(queryClass, predicateLocator);
    }
}
