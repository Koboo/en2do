package eu.koboo.en2do.operators;

import eu.koboo.en2do.mongodb.exception.returntype.MethodReturnTypeException;
import eu.koboo.en2do.utility.parse.ParseUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;

/**
 * Represents the MethodOperator of a method inside a repository.
 */
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public enum MethodOperator {

    /**
     * Searches entities with the given filters.
     */
    FIND(
        "find",
        (method, entityClass, repositoryClass) -> {
            Class<?> entityTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!entityClass.isAssignableFrom(entityTypeClass)) {
                String entityName = entityTypeClass.getSimpleName();
                throw new MethodReturnTypeException(repositoryClass, method,
                    "Collection<" + entityName + "> or " + entityName);
            }
        },
        0
    ),
    /**
     * Deletes all entities with the given filters.
     */
    DELETE(
        "delete",
        (method, entityClass, repositoryClass) -> {
            Class<?> returnTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!Boolean.class.equals(returnTypeClass)) {
                throw new MethodReturnTypeException(repositoryClass, method, "boolean");
            }
        },
        0
    ),
    /**
     * Checks if any entity exists with the given filters.
     */
    EXISTS(
        "exists",
        (method, entityClass, repositoryClass) -> {
            Class<?> returnTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!Boolean.class.equals(returnTypeClass)) {
                throw new MethodReturnTypeException(repositoryClass, method, "boolean");
            }
        },
        0
    ),
    /**
     * Counts all entities with the given filters.
     */
    COUNT(
        "count",
        (method, entityClass, repositoryClass) -> {
            Class<?> returnTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!Long.class.equals(returnTypeClass)) {
                throw new MethodReturnTypeException(repositoryClass, method, "long");
            }
        },
        0
    ),
    /**
     * Updates specific fields on all entities with the given filters.
     */
    UPDATE_FIELD(
        "updateFields",
        (method, entityClass, repositoryClass) -> {
            Class<?> returnTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!Boolean.class.equals(returnTypeClass)) {
                throw new MethodReturnTypeException(repositoryClass, method, "Boolean");
            }
        },
        1
    );

    public static final MethodOperator[] VALUES = MethodOperator.values();

    String keyword;
    ReturnTypeValidator returnTypeValidator;
    int additionalParameters;

    /**
     * Validates the return type of the specific method operator, using the given parameters.
     *
     * @param method          The method, which should be validated
     * @param entityClass     The entity class of the validated repository
     * @param repositoryClass THe repository class
     */
    public void validateReturnType(Method method,
                                   Class<?> entityClass, Class<?> repositoryClass) {
        returnTypeValidator.check(method, entityClass, repositoryClass);
    }
}
