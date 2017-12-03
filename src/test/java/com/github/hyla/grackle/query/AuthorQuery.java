package com.github.hyla.grackle.query;

import com.github.hyla.grackle.annotation.GrackleQuery;
import com.github.hyla.grackle.domain.Author;

@GrackleQuery
public interface AuthorQuery extends EntityQuery<Author, Long, AuthorQuery> {

    AuthorQuery name(String name);

    AuthorQuery nameIs(String name);

    AuthorQuery nameLike(String pattern);
}
