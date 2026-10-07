package eu.koboo.en2do.factory.mapping;

import eu.koboo.en2do.mongodb.exception.RepositoryException;
import eu.koboo.en2do.repository.Repository;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

@UtilityClass
class MappingUtils {

    static ParameterizedType resolveRepositoryType(Class<?> repositoryClass) {
        return Arrays.stream(repositoryClass.getGenericInterfaces())
            .filter(ParameterizedType.class::isInstance)
            .map(ParameterizedType.class::cast)
            .filter(type -> type.getRawType() == Repository.class)
            .findFirst()
            .orElseThrow(() -> new RepositoryException("Couldn't find parameterized types.", repositoryClass));
    }

    static IllegalArgumentException invalidField(Class<?> entityClass, Field field, String message) {
        return new IllegalArgumentException(message + "\n"
            + "  - Entity: " + entityClass.getName() + "\n"
            + "  - Field: " + field.getDeclaringClass().getSimpleName() + "." + field.getName());
    }
}
