package com.github.hyla.grackle.annotation;

import org.springframework.context.annotation.Bean;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Documented
@Bean
public @interface GracklePredicate {

    String[] value() default {};
}
