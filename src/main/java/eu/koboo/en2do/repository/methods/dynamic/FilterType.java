package eu.koboo.en2do.repository.methods.dynamic;

import eu.koboo.en2do.repository.methods.operators.FilterOperator;

import java.lang.reflect.Field;

public record FilterType(Field field, boolean notFilter, FilterOperator operator) {
}
