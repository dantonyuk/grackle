package com.github.hyla.grackle.spring;

import com.github.hyla.grackle.operator.BinaryOperator;
import com.github.hyla.grackle.operator.OperatorLocator;
import com.github.hyla.grackle.operator.TernaryOperator;
import com.github.hyla.grackle.operator.UnaryOperator;
import com.github.hyla.grackle.annotation.GrackleOperator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OperatorBeanFactory implements OperatorLocator {
    @Getter
    private final Map<String, UnaryOperator> unaryOperators = new HashMap<>();
    @Getter
    private final Map<String, BinaryOperator> binaryOperators = new HashMap<>();
    @Getter
    private final Map<String, TernaryOperator> ternaryOperators = new HashMap<>();

    @SuppressWarnings("unused")
    public Object registerOperators(ClassLoader classLoader, Class<?> operatorsClass)
            throws IllegalAccessException, InstantiationException {

        Object operators = operatorsClass.newInstance();
        ReflectionUtils.doWithFields(operatorsClass, new OperatorFieldCallback(operators));
        return operators;
    }

    @AllArgsConstructor
    public class OperatorFieldCallback implements ReflectionUtils.FieldCallback {

        private final Object bean;

        @Override
        public void doWith(Field field) throws IllegalAccessException {
            GrackleOperator annotation = AnnotationUtils.findAnnotation(field, GrackleOperator.class);
            ReflectionUtils.makeAccessible(field);
            Object value = field.get(bean);

            if (annotation == null) {
                return;
            }

            String[] names = (annotation.value().length == 0) ? new String[] { field.getName() } : annotation.value();
            for (String name : names) {
                if (value instanceof UnaryOperator) {
                    log.info("Register unary operator: {}", name);
                    unaryOperators.put(name, (UnaryOperator) value);
                } else if (value instanceof BinaryOperator) {
                    log.info("Register binary operator: {}", name);
                    binaryOperators.put(name, (BinaryOperator) value);
                } else if (value instanceof TernaryOperator) {
                    log.info("Register ternary operator: {}", name);
                    ternaryOperators.put(name, (TernaryOperator) value);
                } else {
                    throw new IllegalArgumentException(
                            "Only unary/binary/ternary operators can be annotated with @" + GrackleOperator.class.getSimpleName());
                }
            }
        }
    }
}
