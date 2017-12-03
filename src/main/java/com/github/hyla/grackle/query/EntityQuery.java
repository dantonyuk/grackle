package com.github.hyla.grackle.query;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * TODO: aliases<br>
 * <pre>
 *  &#64;GrackleQuery(aliases = { &#64;GrackleAlias("customer.company.name", "companyName") })
 *  public interface OrderQuery extends Query<Order, Long, OrderQuery> {
 *      OrderQuery companyNameLike(String pattern);
*   }
 * </pre>
 */
public interface EntityQuery<T, I extends Serializable, Q extends EntityQuery<T, I, Q>> extends Query<T> {

    Optional<T> findById(I id);

    List<T> findByIds(Collection<I> ids);

    // special projections here
//    <R, J extends Serializable, Q2 extends Query<R, J, Q2>> Q2 map(java.util.function.Function<T, R> mapper);
}
