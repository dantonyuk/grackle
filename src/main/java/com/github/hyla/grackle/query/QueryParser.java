package com.github.hyla.grackle.query;

import com.github.hyla.grackle.operator.Operator;
import com.github.hyla.grackle.operator.OperatorLocator;
import com.github.hyla.grackle.util.SplitUtil;
import com.github.hyla.grackle.util.Tuple2;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.JoinType;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.tomcat.util.buf.StringUtils.join;

@Slf4j
public class QueryParser {

    private final Class<? extends EntityQuery> queryClass;
    private final OperatorLocator operatorLocator;
    private Class<?> entityClass;

    public QueryParser(Class<? extends EntityQuery> queryClass, OperatorLocator operatorLocator) {
        this.queryClass = queryClass;
        this.operatorLocator = operatorLocator;
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

    private QueryMethodExecutor parse(Method method, Class<?> entityClass) {
        Optional<Tuple2<List<String>, Operator>> found = SplitUtil
                .findOnRight(method.getName(), operatorLocator::lookup)
                .map(t -> t.map1(x -> SplitUtil.splitWithAliases(x, Collections.emptyMap())));

        if (!found.isPresent()) {
            throw new IllegalStateException("Operation not found in " + method.getName());
        }

        Operator operator = found.get().get_2();
        Optional<PropertyPath> validatedPath = asPropertyPath(found.get().get_1(), entityClass);

        if (!validatedPath.isPresent()) {
            throw new IllegalStateException("Property path not found in " + method.getName());
        }

        PropertyPath path = validatedPath.get();

        return new QueryMethodExecutor(path.name, operator, path.aliases);
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

    @Data
    private static class EntityProperty {
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
                String aliasPath = replaceLastUnderscoreWithDot(aliasName);

                aliases.add(new Alias(aliasName, aliasPath, JoinType.INNER_JOIN));
            }

            name = replaceLastUnderscoreWithDot(join(path, '_'));
        }

        private String replaceLastUnderscoreWithDot(String str) {
            int pos = str.lastIndexOf("_");
            if (pos == -1) {
                return str;
            }

            return new StringBuilder(str).replace(pos, pos + 1, ".").toString();
        }
    }
}
