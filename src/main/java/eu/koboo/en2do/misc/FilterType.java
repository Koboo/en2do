package eu.koboo.en2do.misc;

import java.lang.reflect.Field;

public record FilterType(Field field, FilterOperator operator) {
}