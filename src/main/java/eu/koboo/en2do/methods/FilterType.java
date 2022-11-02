package eu.koboo.en2do.methods;

import java.lang.reflect.Field;

public record FilterType(Field field, FilterOperator operator) {
}
