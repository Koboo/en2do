package eu.koboo.en2do.mongodb.methods.dynamic;

import eu.koboo.en2do.operators.FilterOperator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Represents a segment of a method filter.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class IndexedFilter {

    String bsonFilterFieldKey;
    boolean notFilter;
    FilterOperator operator;
    int nextParameterIndex;
}
