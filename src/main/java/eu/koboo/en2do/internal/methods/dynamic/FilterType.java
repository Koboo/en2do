package eu.koboo.en2do.internal.methods.dynamic;

import eu.koboo.en2do.internal.methods.operators.FilterOperator;

import java.lang.reflect.Field;

/**
 * Represents a segment of a method filter.
 * @param field The field, which should be filtered
 * @param notFilter is true if the filter is negotiated
 * @param operator The operator of the filter
 */
public record FilterType(Field field, boolean notFilter, FilterOperator operator) {
}
