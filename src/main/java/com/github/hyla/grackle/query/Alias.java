package com.github.hyla.grackle.query;

import lombok.Data;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.sql.JoinType;

@Data
public class Alias {
    private final String aliasName;
    private final String associationPath;
    private final JoinType joinType;

    public DetachedCriteria apply(DetachedCriteria criteria) {
        return criteria.createAlias(associationPath, aliasName, joinType);
    }
}
