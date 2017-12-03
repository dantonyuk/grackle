package com.github.hyla.grackle.query;

import com.github.hyla.grackle.domain.Author;
import com.github.hyla.grackle.domain.Book;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class EntityQueryTest {

    @Autowired
    private AuthorQuery authorQuery;

    @Autowired
    private BookQuery bookQuery;

    @Autowired
    private SessionFactory sessionFactory;

    @Before
    public void setUp() {
        Author tolkien = new Author(1L, "Tolkien");
        Book book1 = new Book(1L, "The Lord of the Ring", 5, "AAA", tolkien);
        Book book2 = new Book(2L, "The Hobbit", 3, "BBB", tolkien);

        sessionFactory.getCurrentSession().save(tolkien);
        sessionFactory.getCurrentSession().save(book1);
        sessionFactory.getCurrentSession().save(book2);

        sessionFactory.getCurrentSession().flush();
    }

    @Test
    public void test() {
        assertTrue(authorQuery
                .name("Tolkien")
                .nameLike("%Tol%")
                .exists());

        assertEquals(1, authorQuery
                .nameIs("Tolkien")
                .nameLike("%Tol%")
                .count());

        assertTrue(authorQuery
                .name("Tolkien")
                .nameLike("%Tol%")
                .uniqueResult().isPresent());

        assertEquals(1, bookQuery
                .authorNameIs("Tolkien")
                .titleLike("%Hobbit%")
                .count());

        BookQuery query1 = bookQuery.authorNameIs("Tolkien");
        BookQuery query2 = query1.titleLike("%Hobbit%");

        assertEquals(2, query1.count());
        assertEquals(1, query2.count());

        List<Book> list = bookQuery.isBestseller().list();
        assertEquals(1, list.size());

        Optional<Author> optAuthor = authorQuery.findById(1L);
        assertTrue(optAuthor.isPresent());
        Author author = optAuthor.get();
        list.add(new Book(3L, "Unknown Script", 666, "CCC", author));

        assertEquals(1, bookQuery.isBestseller().list().size());
    }
}
