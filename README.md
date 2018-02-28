Grackle is a library providing query composition in the spring environment.

## Why

One of the main advantages of Spring JPA repositories is possibility to define
queries just by declaring method names. Once you define something like
``findByRatingGeAndAuthor_NameLike`` in the ``BookRepository``, you can use it
like

```java
bookRepository.findByRatingGeAndAuthor_NameLike(Rating.GOOD, "%Grimm")
```

Spring takes care of different kinds of validations: field names, param type
matching etc.

Unfortunately if you need to slightly different query e.g. only in one
predicate, you have to define new method. If you need to extend some queries,
you have to copy-paste them giving rise to combinatorial explosion.

Otherwise, Criteria API provides a way to combine different predicates but is
not as convenient as just declaring one method. Also it is not as good in
validation because it is used only when applied.

Grackle combines advantages from both of the worlds. It provides an easy and
convenient way to build composable queries by declaring methods in the query
interface suppressing com combinatorial explosion.

## How

A ~~picture~~ code is worth a thousand words.

This is how a query is declared in Grackle:

```java
@GrackleQuery
public interface BookQuery extends EntityQuery<Book, Long, BookQuery> {

    BookQuery titleLike(String titlePattern);

    BookQuery authorIs(Author author);

    BookQuery authorNameIs(String name);

    BookQuery ratingGreaterOrEqual(int value);
}
```

This is how a query is used:

```java
List<Book> books = bookQuery
    .titleLike(titlePattern)
    .authorNameIs(authorName)
    .ratingGreaterOrEqual(Rating.GOOD)
    .list();
```

This is the basic using. To get it more, read further.

### Enabling grackle in SpringBoot projects

This is easy. Just use ``@EnableGracleQueries`` annotations:

```java
@SpringBootApplication
@EnableGrackleQueries(basePackages = "com.github.hyla.grackle")
public class GrackleApplication {

    @Bean
    public HibernateJpaSessionFactoryBean sessionFactory() {
        return new HibernateJpaSessionFactoryBean();
    }
}
```

Two things should be mentioned here:

* Grackle relies on Hibernate, it uses hibernate session factory from provided
spring context under the hood. So hibernate session factory should be provided.
* ```@EnableGrackleQueries``` has ```basePackages``` argument defining root
package of the packages that will be scanned for grackle queries.

### Defining grackle query

Code first:

```java
@GrackleQuery
public interface BookQuery extends EntityQuery<Book, Long, BookQuery> {
}
```

As you can see, a grackle query should be marked with ```@GrackleQuery```
annotation in order to be found and registered in the spring container.

Also it is obligatory to extend ```EntityQuery``` with type parameters:
* Type of query's entity
* Type of the ID of query's entity
* Type of the query itself

```EntityQuery``` provides useful methods, e.g. to obtain actual results.

### Defining an operator

An operator is just a method satisfying some rules. Note that all the abstract
methods of the grackle query should be operators.

To be an operator method should have
* return type that is the query type itself
* name which can be splitted to property-name/operation pair
* amount of arguments depending on the operation
* types of arguments depending on the operation and property type

Examples:

```java
OrderQuery orderNumberLike(String orderNumberPattern);
OrderQuery dateBetween(Date from, Date to);
OrderQuery postponedIsTrue();
OrderQuery shippingDateIsNull();
OrderQuery customerIn(List<Customer> customers);
```

If method is not abstract, it should not obey these rules:

```java
default List<Order> unprocessed() {
    return statusId(OrderStatus.NEW).list();
}
```

### Operators

There are a lot of predefined operators:

| Operator | Alternative names |
| --------- | ----------------- |
| eq        | is, '' (empty)    |
| like      |                   |
| in        |                   |
| ge        | greaterOrEqual    |

The list will be wider in future.

You (will) can define your own operators. See how operators are defined now:

```java
@GrackleOperators("grackleDefaultOperators")
public class Operators implements OperatorLocator {

    @GrackleOperator
    private UnaryOperator isNull = unaryOperator(Restrictions::isNull);

    @GrackleOperator({ "eq", "is", "" })
    private BinaryOperator eq = binaryOperator(Restrictions::eq);

    @GrackleOperator("like")
    private BinaryOperator like = binaryOperator(Restrictions::like);

    @GrackleOperator("in")
    private BinaryOperator<Collection> in = binaryOperator(Restrictions::in);

    @GrackleOperator
    private TernaryOperator between = ternaryOperator(Restrictions::between);

    @GrackleOperator({"ge", "greaterOrEqual"})
    private BinaryOperator ge = binaryOperator(Restrictions::ge);
}
```

Note that the class itself is marked with ```@GrackleOperators``` annotation
and implements ```OperatorLocator``` interface.

### Query and EntityQuery interfaces

```Query``` allows to get
* unique result
* first result
* list of records
* list of records limited by first and max results
* count of records
Also it allows to check if some record exists.

```EntityQuery``` allows to find entity (or list of entities) by id (or list)
of ids).

