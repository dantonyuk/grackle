package com.github.hyla.grackle.spring;

import com.github.hyla.grackle.annotation.GrackleOperators;
import com.github.hyla.grackle.annotation.EnableGrackleQueries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Map;

@Slf4j
public class OperatorDefinitionRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private ClassLoader beanClassLoader;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        log.debug("Operator definition: {}", metadata.getClassName());

        Map<String, Object> annotationAttributes = metadata
                .getAnnotationAttributes(EnableGrackleQueries.class.getName());
        String[] basePackages = (String[]) annotationAttributes.get("basePackages");
        if (basePackages == null || basePackages.length == 0) {
            String configPackage = metadata.getClassName().replaceFirst("\\.[^.]+$", "");
            basePackages = new String[] { configPackage };
        }

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        AnnotationMetadata meta = beanDefinition.getMetadata();
                        return meta.isIndependent() && !meta.isAbstract();
                    }
                };
        scanner.addIncludeFilter(new AnnotationTypeFilter(GrackleOperators.class));

        Arrays.stream(basePackages)
                .flatMap(basePackage -> scanner.findCandidateComponents(basePackage).stream())
                .forEach(beanDefinition -> {
                    // I'm not sure what class loader should be used here
                    Class<?> clazz = ClassUtils
                            .resolveClassName(beanDefinition.getBeanClassName(), beanClassLoader);

                    String beanName = ClassUtils.getShortNameAsProperty(clazz);
                    log.debug("Operators bean: {}", beanName);

                    ConstructorArgumentValues args = new ConstructorArgumentValues();
                    args.addGenericArgumentValue(beanClassLoader);
                    args.addGenericArgumentValue(clazz);

                    GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
                    proxyBeanDefinition.setBeanClass(clazz);
                    proxyBeanDefinition.setConstructorArgumentValues(args);
                    proxyBeanDefinition.setFactoryBeanName("grackleOperatorBeanFactory");
                    proxyBeanDefinition.setFactoryMethodName("registerOperators");

                    registry.registerBeanDefinition(beanName, proxyBeanDefinition);
                });
    }
}
