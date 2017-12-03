package com.github.hyla.grackle;

import com.github.hyla.grackle.annotation.EnableGrackleQueries;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;

@SpringBootApplication
@EnableGrackleQueries(basePackages = "com.github.hyla.grackle")
public class GrackleApplicationTests {

    @Bean
    public HibernateJpaSessionFactoryBean sessionFactory() {
        return new HibernateJpaSessionFactoryBean();
    }
}
