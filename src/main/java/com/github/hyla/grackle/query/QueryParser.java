package com.github.hyla.grackle.query;

import com.github.hyla.grackle.predicate.PredicateLocator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.JoinType;
import org.springframework.data.util.Pair;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.apache.tomcat.util.buf.StringUtils.join;
import static org.springframework.util.StringUtils.collectionToDelimitedString;
import static org.springframework.util.StringUtils.uncapitalize;

@Slf4j
public class QueryParser {

    private final Class<? extends EntityQuery> queryClass;
    private final PredicateLocator predicateLocator;
    private Class<?> entityClass;

    public QueryParser(Class<? extends EntityQuery> queryClass, PredicateLocator predicateLocator) {
        this.queryClass = queryClass;
        this.predicateLocator = predicateLocator;
        init();
    }

    private void init() {
        for (AnnotatedType queryInterface : queryClass.getAnnotatedInterfaces()) {
            Type interfaceType = queryInterface.getType();
            if (interfaceType instanceof ParameterizedType) {
                ParameterizedType queryParamType = (ParameterizedType) interfaceType;
                parseQueryInheritance(queryParamType);
            }
        }

        if (entityClass == null) {
            throw new IllegalStateException("Query class should implement Query interface");
        }
    }

    private void parseQueryInheritance(ParameterizedType queryParamType) {
        if (queryParamType.getRawType() != EntityQuery.class) {
            return;
        }

        Type[] paramTypes = queryParamType.getActualTypeArguments();
        if (!(paramTypes[0] instanceof Class)) {
            throw new IllegalStateException("Entity type should be class");
        }

        if (!(paramTypes[1] instanceof Class)) {
            throw new IllegalStateException("Id type should be class");
        }

        if (paramTypes[2] != queryClass) {
            throw new IllegalStateException("Query type should be the same query class");
        }

        entityClass = (Class) paramTypes[0];
    }

    public Map<Method, QueryMethodExecutor> parse() {
        Map<Method, QueryMethodExecutor> executors = new HashMap<>();

        for (Method method : queryClass.getDeclaredMethods()) {
            if (method.isDefault()) {
                continue;
            }

            if (queryClass != method.getReturnType()) {
                throw new IllegalStateException("Method should return the same type");
            }

            executors.put(method, parse(method, entityClass));
        }

        return executors;
    }

//    private Stream<List<List<String>>> foo(List<String> list) {
//        return subsequences(
//                IntStream.range(1, list.size()).boxed())
//                .map(places -> splitPlaces(places.collect(Collectors.toList()), list)));
//    }
//
//    private <T> Stream<Stream<T>> subsequences(Stream<T> s) {
//
//    }

    private <T> Stream<List<T>> splitPlaces(List<Integer> places, List<T> s) {
        if (places.isEmpty()) return Stream.of(s);
        if (s.isEmpty()) return Stream.empty();
        int place = places.get(0);
        List<T> l = s.subList(0, place);
        List<T> r = s.subList(place, s.size());
        return Stream.concat(
                Stream.of(l),
                splitPlaces(places.stream().map(x -> x - place).collect(Collectors.toList()), r));
    }

    private QueryMethodExecutor parse(Method method, Class<?> entityClass) {
        List<String> words = Arrays.asList(method.getName().split("(?<!^)(?=[A-Z])"));
        int wordCount = words.size();
        Optional<QueryMethodExecutor> parsed = IntStream.range(0, wordCount).boxed()
                .map(i -> Pair.of(
                        words.subList(0, wordCount - i),            // candidate for property path
                        words.subList(wordCount - i, wordCount)))   // candidate for operation
                .map(p -> prepareExecutor(p.getFirst(), joinWords(p.getSecond()), entityClass))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        if (!parsed.isPresent()) {
            throw new IllegalStateException("Property path and/or operation not found in " + method.getName());
        }

        return parsed.get();
    }

    private String joinWords(List<String> words) {
        return uncapitalize(collectionToDelimitedString(words, ""));
    }

    private Optional<QueryMethodExecutor> prepareExecutor(
            List<String> propertyPath, String predicateName, Class<?> entityClass) {

        List<String> correctedPropertyPath = propertyPath.stream()
                .map(StringUtils::uncapitalize)
                .collect(Collectors.toList());

        return asPropertyPath(correctedPropertyPath, entityClass).flatMap(
                prop -> predicateLocator.lookup(predicateName).map(
                        predicate -> new QueryMethodExecutor(prop.name, predicate, prop.aliases)));
    }

    private Optional<PropertyPath> asPropertyPath(List<String> propertyPath, Class<?> entityClass) {
        Class<?> propertyHolderClass = entityClass;
        for (String propertyName : propertyPath) {
            Optional<EntityProperty> optionalProperty = findProperty(propertyHolderClass, propertyName);
            if (!optionalProperty.isPresent()) {
                return Optional.empty();
            }

            EntityProperty property = optionalProperty.get();

            propertyHolderClass = property.getPropertyClass();
        }

        return Optional.of(new PropertyPath(propertyPath));
    }

    private Optional<EntityProperty> findProperty(Class<?> clz, String propertyName) {
        // current implementation is looking for the fields only
        // in further releases it should search for aliases as well
        return Optional.ofNullable(ReflectionUtils.findField(clz, propertyName))
                .map(field -> new EntityProperty(propertyName, field.getType()));
    }

    private String toPropertyName(List<String> propertyPath) {
        return uncapitalize(propertyPath.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
    }

    @Data
    private class EntityProperty {
        private final String name;
        private final Class<?> propertyClass;
    }

    @Data
    private static class PropertyPath {
        private final String name;
        private final List<Alias> aliases;

        PropertyPath(List<String> path) {
            aliases = new ArrayList<>(Math.max(0, path.size() - 4));
            for (int i = 1; i < path.size(); i++) {
                String aliasName = join(path.subList(0, i), '_');
                // haha! sorry for that, will be replaced in future
                String aliasPath = reverse(reverse(aliasName).replaceFirst("_", "."));
                aliases.add(new Alias(aliasName, aliasPath, JoinType.INNER_JOIN));
            }

            name = reverse(reverse(join(path, '_').replaceFirst("_", ".")));
        }

        private String reverse(String s) {
            return new StringBuilder(s).reverse().toString();
        }
    }
}
