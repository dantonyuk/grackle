package com.github.hyla.grackle.operator;

import java.util.Map;
import java.util.Optional;

public interface OperatorLocator {

    Map<String, UnaryOperator> getUnaryOperators();
    Map<String, BinaryOperator> getBinaryOperators();
    Map<String, TernaryOperator> getTernaryOperators();

    default Optional<Operator> lookup(String operatorName) {
        Operator operator = getUnaryOperators().get(operatorName);
        if (operator != null) return Optional.of(operator);
        operator = getBinaryOperators().get(operatorName);
        if (operator != null) return Optional.of(operator);
        operator = getTernaryOperators().get(operatorName);
        return Optional.ofNullable(operator);
    }
}
