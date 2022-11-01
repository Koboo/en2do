package eu.koboo.en2do.validation;

import java.lang.reflect.Field;

public record FilterType(Field field, FilterOperator operator) {
}
