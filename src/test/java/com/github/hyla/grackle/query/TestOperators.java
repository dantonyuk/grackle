package com.github.hyla.grackle.query;

import com.github.hyla.grackle.annotation.GrackleOperator;
import com.github.hyla.grackle.annotation.GrackleOperators;
import com.github.hyla.grackle.operator.UnaryOperator;
import com.github.hyla.grackle.spring.Operators;
import org.hibernate.criterion.Restrictions;

@GrackleOperators
public class TestOperators {

    @GrackleOperator
    private UnaryOperator notNull = Operators.unaryOperator(Restrictions::isNotNull);
}
