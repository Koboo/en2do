package eu.koboo.en2do.internal.methods.dynamic;

import eu.koboo.en2do.internal.methods.operators.FilterOperator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;

/**
 * Represents a segment of a method filter.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class FilterType {

    /**
     * The field, which should be filtered
     */
    Field field;
    /**
     * is true if the filter is negotiated
     */
    boolean notFilter;
    /**
     * The operator of the filter
     */
    FilterOperator operator;
}
