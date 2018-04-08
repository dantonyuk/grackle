package com.github.hyla.grackle.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@Slf4j
public class QueryProxy implements InvocationHandler {

    private final Class<? extends EntityQuery> queryClass;
    private final QueryImpl query;
    private final QueryParser parser;
    private Map<Method, QueryMethodExecutor> executorMap;
    private Constructor<MethodHandles.Lookup> constructor;

    public QueryProxy(Class<? extends EntityQuery> queryClass, QueryImpl original, QueryParser parser) {
        this(queryClass, original, parser, null);

        try {
            constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e); // should not be thrown
        }

        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
    }

    private QueryProxy(Class<? extends EntityQuery> queryClass, QueryImpl original, QueryParser parser, Map<Method, QueryMethodExecutor> executorMap) {
        this.queryClass = queryClass;
        this.query = original;
        this.parser = parser;
        this.executorMap = executorMap;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.debug("Invoking {} for {}", method.getName(), proxy.getClass().getName());

        // if this is not a Query class method, just invoke and return
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass != queryClass) {
            return method.invoke(query, args);
        }

        // same
        if (method.isDefault()) {
            ReflectionUtils.makeAccessible(method);
            return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        }

        if (queryClass != method.getReturnType()) {
            throw new IllegalStateException("Method should return the same type");
        }

        // TODO: should be moved to @PostConstruct
        if (executorMap == null) {
            executorMap = parser.parse();
        }

        @SuppressWarnings("unchecked")
        QueryImpl nextQuery = executorMap.get(method).apply(query, args);
        return Proxy.newProxyInstance(queryClass.getClassLoader(), new Class[] { queryClass },
                new QueryProxy(queryClass, nextQuery, null, executorMap));
    }
}
