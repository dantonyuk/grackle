package com.github.hyla.grackle.query;

import com.github.hyla.grackle.annotation.GrackleQuery;
import com.github.hyla.grackle.annotation.WithAlias;
import com.github.hyla.grackle.annotation.WithAliases;
import com.github.hyla.grackle.domain.Author;
import com.github.hyla.grackle.domain.Book;

@GrackleQuery
//@GrackleAlias(name = "authorName", value = "author.name")
public interface BookQuery extends EntityQuery<Book, Long, BookQuery> {

    BookQuery titleIs(String title);

    BookQuery titleNotNull();

    BookQuery titleLike(String title);

    BookQuery authorIs(Author author);

    BookQuery author_nameIs(String name);

    BookQuery ratingGreaterOrEqual(int value);

    @WithAlias(name="creator", path="author.name")
    BookQuery creatorIs(String name);

    @WithAlias(name="writer", path="author")
    @WithAlias(name="penname", path="name")
    BookQuery writer_penname(String name);

    @WithAliases({
        @WithAlias(name="writer", path="author"),
        @WithAlias(name="thename", path="name")
    })
    BookQuery writer_thename(String name);

    default BookQuery isBestseller() {
        return ratingGreaterOrEqual(5);
    }
}
