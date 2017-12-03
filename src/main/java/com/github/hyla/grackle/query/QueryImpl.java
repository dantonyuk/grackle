package com.github.hyla.grackle.query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class QueryImpl<T, I extends Serializable, Q extends QueryImpl<T, I, Q>> implements EntityQuery<T, I, Q> {

    private final Class<T> entityClass;
    private final Class<I> idClass;
    private final SessionProvider sessionProvider;
    private final Set<Alias> aliases = new LinkedHashSet<>();
    private final List<DetachedCriteriaTransformer> transformers = new LinkedList<>();

    public QueryImpl(Class<T> entityClass, Class<I> idClass, SessionProvider sessionProvider) {
        this.entityClass = entityClass;
        this.idClass = idClass;
        this.sessionProvider = sessionProvider;
    }

    @Override
    public Optional<T> findById(I id) {
        return Optional.ofNullable(getSession().get(entityClass, id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findByIds(Collection<I> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // TODO: support other names of pk property
        return (List<T>) getSession().createCriteria(entityClass).add(Restrictions.in("id", ids)).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> uniqueResult() {
        return (Optional<T>) Optional.ofNullable(executable().uniqueResult());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> first() {
        List<T> list = executable().setMaxResults(1).list();
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> list() {
        return (List<T>) executable().list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> list(int limit) {
        return (List<T>) executable().setMaxResults(limit).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> list(int first, int limit) {
        return (List<T>) executable().setFirstResult(first).setMaxResults(limit).list();
    }

    @Override
    public long count() {
        return ((Number) executable().setProjection(Projections.rowCount()).uniqueResult()).longValue();
    }

    @Override
    public boolean exists() {
        return !executable().setMaxResults(1).list().isEmpty();
    }

    // package methods region

    QueryImpl<T, I, Q> copyWith(List<Alias> newAliases, DetachedCriteriaTransformer transformer) {
        QueryImpl<T, I, Q> copy = new QueryImpl<>(entityClass, idClass, sessionProvider);

        for (Alias newAlias : newAliases) {
            if (!copy.aliases.contains(newAlias)) {
                copy.aliases.add(newAlias);
            }
        }

        copy.transformers.add(transformer);
        return copy;
    }

    // private methods region

    private Session getSession() {
        return sessionProvider.getSession();
    }

    private DetachedCriteria apply() {
        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);

        for (Alias alias : aliases) {
            alias.apply(criteria);
        }

        for (DetachedCriteriaTransformer transformer : transformers) {
            transformer.transform(criteria);
        }

        return criteria;
    }

    private Criteria executable() {
        return apply().getExecutableCriteria(getSession());
    }
}
