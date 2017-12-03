package com.github.hyla.grackle.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrackleConfig {

//    @Bean(name = "gracklePredicateBeanFactory")
//    public PredicateBeanFactory predicateBeanFactory() {
//        return new PredicateBeanFactory();
//    }

    @Bean(name = "grackleQueryBeanFactory")
    public QueryBeanFactory queryBeanFactory() {
        return new QueryBeanFactory();
    }

    @Bean(name = "grackleQueryParserFactory")
    public QueryParserFactory queryParserFactory() {
        return new QueryParserFactory();
    }
//
//    @Bean(name = "grackleDefaultPredicates")
//    public Predicates predicates() {
//        return new Predicates();
//    }
}
