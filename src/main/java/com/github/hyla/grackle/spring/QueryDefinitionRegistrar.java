package com.github.hyla.grackle.spring;

import com.github.hyla.grackle.annotation.GrackleQuery;
import com.github.hyla.grackle.annotation.EnableGrackleQueries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Map;

@Slf4j
public class QueryDefinitionRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private ClassLoader beanClassLoader;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        log.debug("Query definition: {}", metadata.getClassName());

        Map<String, Object> annotationAttributes = metadata
                .getAnnotationAttributes(EnableGrackleQueries.class.getName());
        String[] basePackages = (String[]) annotationAttributes.get("basePackages");
        if (basePackages == null || basePackages.length == 0) {
            String configPackage = metadata.getClassName().replaceFirst("\\.[^.]+$", "");
            basePackages = new String[] { configPackage };
        }

        GrackleClassPathScanner scanner = new GrackleClassPathScanner(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(GrackleQuery.class));

        Arrays.stream(basePackages).flatMap(basePackage -> scanner.findCandidateComponents(basePackage).stream())
                .forEach(beanDefinition -> {
                    Class<?> clazz = ClassUtils
                            .resolveClassName(beanDefinition.getBeanClassName(), beanClassLoader);

                    String beanName = ClassUtils.getShortNameAsProperty(clazz);
                    log.debug("Query bean: {}", beanName);

                    ConstructorArgumentValues args = new ConstructorArgumentValues();
                    args.addGenericArgumentValue(beanClassLoader);
                    args.addGenericArgumentValue(clazz);

                    GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
                    proxyBeanDefinition.setBeanClass(clazz);
                    proxyBeanDefinition.setConstructorArgumentValues(args);
                    proxyBeanDefinition.setFactoryBeanName("grackleQueryBeanFactory");
//                    proxyBeanDefinition.setDependsOn("grackleDefaultOperators");
                    proxyBeanDefinition.setFactoryMethodName("createQueryProxyBean");

                    registry.registerBeanDefinition(beanName, proxyBeanDefinition);
                });
    }
}
