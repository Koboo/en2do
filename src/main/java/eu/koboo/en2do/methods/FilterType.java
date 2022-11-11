package eu.koboo.en2do.methods;

import eu.koboo.en2do.methods.operators.FilterOperator;

import java.lang.reflect.Field;

public record FilterType(Field field, boolean notFilter, FilterOperator operator) {
}
