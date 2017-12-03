package com.github.hyla.grackle.spring;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

public class GrackleClassPathScanner extends ClassPathScanningCandidateComponentProvider {

    public GrackleClassPathScanner(boolean useDefaultFilters) {
        super(useDefaultFilters);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isIndependent() && beanDefinition.getMetadata().isInterface();
    }
}
