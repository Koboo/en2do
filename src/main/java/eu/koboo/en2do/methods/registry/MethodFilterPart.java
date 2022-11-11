package eu.koboo.en2do.methods.registry;

import eu.koboo.en2do.methods.FilterType;

public record MethodFilterPart(FilterType filterType, int nextParameterIndex) {
}
