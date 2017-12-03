package com.github.hyla.grackle.annotation;

import com.github.hyla.grackle.spring.GrackleConfig;
import com.github.hyla.grackle.spring.QueryDefinitionRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ GrackleConfig.class,
//        PredicateDefinitionRegistrar.class,
        QueryDefinitionRegistrar.class })
public @interface EnableGrackleQueries {

    @AliasFor("basePackages") String[] value() default {};

    @AliasFor("value") String[] basePackages() default {};
}
