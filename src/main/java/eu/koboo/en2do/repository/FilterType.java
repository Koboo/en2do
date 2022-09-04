package eu.koboo.en2do.repository;

import java.lang.reflect.Field;

public record FilterType(Field field, FilterOperator operator) {
}