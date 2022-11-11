package eu.koboo.en2do.meta.registry;

import eu.koboo.en2do.meta.operators.FilterOperator;

import java.lang.reflect.Field;

public record FilterType(Field field, boolean notFilter, FilterOperator operator) {
}
