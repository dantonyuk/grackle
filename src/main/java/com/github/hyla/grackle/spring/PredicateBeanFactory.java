package com.github.hyla.grackle.spring;

import com.github.hyla.grackle.predicate.BinaryPredicate;
import com.github.hyla.grackle.predicate.TernaryPredicate;
import com.github.hyla.grackle.predicate.UnaryPredicate;
import com.github.hyla.grackle.annotation.GracklePredicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
//@Component("gracklePredicateBeanFactory")
public class PredicateBeanFactory /*implements PredicateLocator*/ {
    @Getter
    private final Map<String, UnaryPredicate> unaryPredicates = new HashMap<>();
    @Getter
    private final Map<String, BinaryPredicate> binaryPredicates = new HashMap<>();
    @Getter
    private final Map<String, TernaryPredicate> ternaryPredicates = new HashMap<>();

    @SuppressWarnings("unused")
    public Object registerPredicates(ClassLoader classLoader, Class<?> predicatesClass)
            throws IllegalAccessException, InstantiationException {

        Object predicates = predicatesClass.newInstance();
        ReflectionUtils.doWithFields(predicatesClass, new PredicateFieldCallback(predicates));
        return predicates;
    }

    @AllArgsConstructor
    public class PredicateFieldCallback implements ReflectionUtils.FieldCallback {

        private final Object bean;

        @Override
        public void doWith(Field field) throws IllegalAccessException {
            GracklePredicate annotation = AnnotationUtils.findAnnotation(field, GracklePredicate.class);
            ReflectionUtils.makeAccessible(field);
            Object value = field.get(bean);

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
