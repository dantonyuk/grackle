package com.github.hyla.grackle.spring;

import com.github.hyla.grackle.query.EntityQuery;
import com.github.hyla.grackle.query.QueryImpl;
import com.github.hyla.grackle.query.QueryProxy;
import com.github.hyla.grackle.query.SessionProvider;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

@DependsOn("gracklePredicateBeanFactory")
public class QueryBeanFactory implements SessionProvider {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private QueryParserFactory queryParserFactory;

    @Override
    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @SuppressWarnings({ "unchecked", "unused" })
    public <T extends EntityQuery> T createQueryProxyBean(ClassLoader classLoader, Class<T> queryClass) {
        if (!queryClass.isInterface()) {
            throw new IllegalArgumentException("Class " + queryClass.getName() + " is not an interface");
        }

        for (AnnotatedType intf : queryClass.getAnnotatedInterfaces()) {
            if (intf.getType() instanceof ParameterizedType) {
                ParameterizedType queryType = (ParameterizedType) intf.getType();
                if (EntityQuery.class == queryType.getRawType()) {
                    Type[] typeArguments = queryType.getActualTypeArguments();

                    assertArgumentsAreRealClasses(typeArguments);
                    assertThirdParameterIsSameQueryClass(typeArguments, queryClass);

                    return (T) Proxy.newProxyInstance(classLoader, new Class[] { queryClass }, new QueryProxy(
                            queryClass,
                            new QueryImpl((Class) typeArguments[0], (Class) typeArguments[1], this),
                            queryParserFactory.newParser(queryClass).parse()));
                }
            }
        }

        throw new IllegalArgumentException("The class " + queryClass.getName() + " does not extends Query interface");
    }

    private <T extends EntityQuery> void assertThirdParameterIsSameQueryClass(Type[] typeArguments, Class<T> cls) {
        if (typeArguments[2] != cls) {
            throw new IllegalStateException("Third type argument should be the current query class");
        }
    }

    private void assertArgumentsAreRealClasses(Type[] typeArguments) {
        for (Type typeArgument : typeArguments) {
            if (!(typeArgument instanceof Class)) {
                throw new IllegalStateException("Query arguments should be Class instances");
            }
        }
    }
}
