package com.github.hyla.grackle.query;

import com.github.hyla.grackle.annotation.GrackleQuery;
import com.github.hyla.grackle.domain.Author;
import com.github.hyla.grackle.domain.Book;

@GrackleQuery
//@GrackleAlias(name = "authorName", value = "author.name")
public interface BookQuery extends EntityQuery<Book, Long, BookQuery> {

    BookQuery titleIs(String title);

    BookQuery titleLike(String title);

    BookQuery authorIs(Author author);

    BookQuery author_nameIs(String name); // should be author_name

    BookQuery ratingGreaterOrEqual(int value);

//
//    BookQuery articleNumber(String articleNumber);
//

//    @WithAlias("author.name", "creator")
//    BookQuery creatorIs(String name);

    default BookQuery isBestseller() {
        return ratingGreaterOrEqual(5);
    }
}
