package com.github.hyla.grackle.spring;

import com.github.hyla.grackle.predicate.BinaryPredicate;
import com.github.hyla.grackle.predicate.PredicateLocator;
import com.github.hyla.grackle.predicate.TernaryPredicate;
import com.github.hyla.grackle.predicate.UnaryPredicate;
import com.github.hyla.grackle.annotation.GracklePredicate;
import com.github.hyla.grackle.annotation.GracklePredicates;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@GracklePredicates("grackleDefaultPredicates")
@SuppressWarnings("unused")
@Slf4j
public class Predicates implements PredicateLocator {

    @GracklePredicate
    private UnaryPredicate isNull = unaryPredicate(Restrictions::isNull);

    @GracklePredicate({ "eq", "is", "" })
    private BinaryPredicate eq = binaryPredicate(Restrictions::eq);

    @GracklePredicate("like")
    private BinaryPredicate like = binaryPredicate(Restrictions::like);

    @GracklePredicate("in")
    private BinaryPredicate<Collection> in = binaryPredicate(Restrictions::in);

    @GracklePredicate
    private TernaryPredicate between = ternaryPredicate(Restrictions::between);

    @GracklePredicate({"ge", "greaterOrEqual"})
    private BinaryPredicate ge = binaryPredicate(Restrictions::ge);

    ////////////////////////////////////////////////////

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {

        R apply(T t, U u, V v);
    }

    public static UnaryPredicate unaryPredicate(Function<String, Criterion> restriction) {
        return (criteria, propertyName) -> criteria.add(restriction.apply(propertyName));
    }

    public static <T> BinaryPredicate<T> binaryPredicate(BiFunction<String, T, Criterion> restriction) {
        return (DetachedCriteria criteria, String propertyName, T value) -> criteria.add(restriction.apply(propertyName, value));
    }

    public static <T> TernaryPredicate<T> ternaryPredicate(TriFunction<String, T, T, Criterion> restriction) {
        return (criteria, propertyName, left, right) -> criteria.add(restriction.apply(propertyName, left, right));
    }

    /////////////////////////////////////////////////////

    // hack

    @Getter
    private final Map<String, UnaryPredicate> unaryPredicates = new HashMap<>();
    @Getter
    private final Map<String, BinaryPredicate> binaryPredicates = new HashMap<>();
    @Getter
    private final Map<String, TernaryPredicate> ternaryPredicates = new HashMap<>();

    {
        ReflectionUtils.doWithFields(getClass(), new PredicateFieldCallback());
    }

    @AllArgsConstructor
    public class PredicateFieldCallback implements ReflectionUtils.FieldCallback {

        @Override
        public void doWith(Field field) throws IllegalAccessException {
            GracklePredicate annotation = AnnotationUtils.findAnnotation(field, GracklePredicate.class);
            ReflectionUtils.makeAccessible(field);
            Object value = field.get(Predicates.this);

            if (annotation == null) {
                return;
            }

            String[] names = (annotation.value().length == 0) ? new String[] { field.getName() } : annotation.value();
            for (String name : names) {
                if (value instanceof UnaryPredicate) {
                    log.info("Register unary predicate: {}", name);
                    unaryPredicates.put(name, (UnaryPredicate) value);
                } else if (value instanceof BinaryPredicate) {
                    log.info("Register binary predicate: {}", name);
                    binaryPredicates.put(name, (BinaryPredicate) value);
                } else if (value instanceof TernaryPredicate) {
                    log.info("Register ternary predicate: {}", name);
                    ternaryPredicates.put(name, (TernaryPredicate) value);
                } else {
                    throw new IllegalArgumentException(
                            "Only unary/binary/ternary predicates can be annotated with @" + GracklePredicate.class.getSimpleName());
                }
            }
        }
    }
}
