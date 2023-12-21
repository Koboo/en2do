package eu.koboo.en2do.mongodb.methods.dynamic;

import eu.koboo.en2do.mongodb.methods.dynamic.FilterType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Represents a filter part of the method.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class MethodFilterPart {

    /**
     * The type of the filter
     */
    FilterType filterType;

    /**
     * The parameter index
     */
    int nextParameterIndex;
}
