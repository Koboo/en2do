package eu.koboo.en2do.mongodb.methods.dynamic;

import eu.koboo.en2do.internal.operators.Chain;
import eu.koboo.en2do.internal.operators.FilterOperator;
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
public class IndexedFilter {

    String bsonName;
    boolean notFilter;
    FilterOperator operator;
    int nextParameterIndex;
}
