package com.github.hyla.grackle.spring;

import com.github.hyla.grackle.operator.BinaryOperator;
import com.github.hyla.grackle.operator.TernaryOperator;
import com.github.hyla.grackle.operator.UnaryOperator;
import com.github.hyla.grackle.annotation.GrackleOperator;
import com.github.hyla.grackle.annotation.GrackleOperators;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

@GrackleOperators
@SuppressWarnings("unused")
@Slf4j
public class Operators {

    @GrackleOperator
    private UnaryOperator isNull = unaryOperator(Restrictions::isNull);

    @GrackleOperator
    private UnaryOperator isNotNull = unaryOperator(Restrictions::isNotNull);

    @GrackleOperator
    private UnaryOperator isEmpty = unaryOperator(Restrictions::isEmpty);

    @GrackleOperator
    private UnaryOperator isNotEmpty = unaryOperator(Restrictions::isNotEmpty);

    @GrackleOperator({ "eq", "is", "" })
    private BinaryOperator eq = binaryOperator(Restrictions::eq);

    @GrackleOperator({ "ne", "isNot" })
    private BinaryOperator ne = binaryOperator(Restrictions::ne);

    @GrackleOperator
    private BinaryOperator like = binaryOperator(Restrictions::like);

    @GrackleOperator
    private BinaryOperator ilike = binaryOperator(Restrictions::ilike);

    @GrackleOperator
    private BinaryOperator startsWith = binaryOperator(
            (name, value) -> Restrictions.like(name, (String) value, MatchMode.START));

    @GrackleOperator
    private BinaryOperator endsWith = binaryOperator(
            (name, value) -> Restrictions.like(name, (String) value, MatchMode.END));

    @GrackleOperator
    private BinaryOperator istartsWith = binaryOperator(
            (name, value) -> Restrictions.ilike(name, (String) value, MatchMode.START));

    @GrackleOperator
    private BinaryOperator iendsWith = binaryOperator(
            (name, value) -> Restrictions.ilike(name, (String) value, MatchMode.END));

    @GrackleOperator
    private BinaryOperator eqOrIsNull = binaryOperator(Restrictions::eqOrIsNull);

    @GrackleOperator
    private BinaryOperator neOrIsNotNull = binaryOperator(Restrictions::neOrIsNotNull);

    @GrackleOperator({"ge", "greaterOrEqual"})
    private BinaryOperator ge = binaryOperator(Restrictions::ge);

    @GrackleOperator({"gt", "greaterThan"})
    private BinaryOperator gt = binaryOperator(Restrictions::gt);

    @GrackleOperator({"le", "lessOrEqual"})
    private BinaryOperator le = binaryOperator(Restrictions::le);

    @GrackleOperator({"lt", "lessThan"})
    private BinaryOperator lt = binaryOperator(Restrictions::lt);

    @GrackleOperator("in")
    private BinaryOperator<Collection> in = binaryOperator(Restrictions::in);

    @GrackleOperator
    private TernaryOperator between = ternaryOperator(Restrictions::between);

    ////////////////////////////////////////////////////

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {

        R apply(T t, U u, V v);
    }

    public static UnaryOperator unaryOperator(Function<String, Criterion> restriction) {
        return (criteria, propertyName) -> criteria.add(restriction.apply(propertyName));
    }

    public static <T> BinaryOperator<T> binaryOperator(BiFunction<String, T, Criterion> restriction) {
        return (DetachedCriteria criteria, String propertyName, T value) -> criteria.add(restriction.apply(propertyName, value));
    }

    public static <T> TernaryOperator<T> ternaryOperator(TriFunction<String, T, T, Criterion> restriction) {
        return (criteria, propertyName, left, right) -> criteria.add(restriction.apply(propertyName, left, right));
    }
}
