package eu.koboo.en2do.internal.methods.dynamic;

/**
 * Represents a filter part of the method.
 * @param filterType The type of the filter
 * @param nextParameterIndex The parameter index
 */
public record MethodFilterPart(FilterType filterType, int nextParameterIndex) {
}
