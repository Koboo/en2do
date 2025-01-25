package eu.koboo.en2do.operators;

import eu.koboo.en2do.mongodb.exception.returntype.*;
import eu.koboo.en2do.utility.parse.ParseUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

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
                throw new MethodFindReturnTypeException(method, entityClass, repositoryClass);
            }
        }
    ),
    /**
     * Deletes all entities with the given filters.
     */
    DELETE(
        "delete",
        (method, entityClass, repositoryClass) -> {
            Class<?> returnTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!Boolean.class.equals(returnTypeClass)) {
                throw new MethodBooleanReturnTypeException(method, repositoryClass);
            }
        }
    ),
    /**
     * Checks if any entity exists with the given filters.
     */
    EXISTS(
        "exists",
        (method, entityClass, repositoryClass) -> {
            Class<?> returnTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!Boolean.class.equals(returnTypeClass)) {
                throw new MethodBooleanReturnTypeException(method, repositoryClass);
            }
        }
    ),
    /**
     * Counts all entities with the given filters.
     */
    COUNT(
        "count",
        (method, entityClass, repositoryClass) -> {
            Class<?> returnTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!Long.class.equals(returnTypeClass)) {
                throw new MethodLongReturnTypeException(method, repositoryClass);
            }
        }
    ),
    /**
     * Creates pagination on all entities with the given filters.
     */
    PAGE(
        "page",
        (method, entityClass, repositoryClass) -> {
            ParameterizedType parameterizedType = ParseUtils.decapsulateFuture(method);
            Class<?> parameterizedReturnClass = (Class<?>) parameterizedType.getRawType();
            if (!Collection.class.isAssignableFrom(parameterizedReturnClass)) {
                throw new MethodFindListReturnTypeException(method, entityClass, repositoryClass);
            }

            Class<?> returnTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!returnTypeClass.isAssignableFrom(entityClass)) {
                throw new MethodFindListTypeException(method, repositoryClass, returnTypeClass, entityClass);
            }
        }
    ),
    /**
     * Updates specific fields on all entities with the given filters.
     */
    UPDATE_FIELD(
        "updateFields",
        (method, entityClass, repositoryClass) -> {
            Class<?> returnTypeClass = ParseUtils.parseValidatableReturnType(method);
            if (!Boolean.class.equals(returnTypeClass)) {
                throw new MethodBooleanReturnTypeException(method, repositoryClass);
            }
        }
    );

    public static final MethodOperator[] VALUES = MethodOperator.values();

    String keyword;
    ReturnTypeValidator returnTypeValidator;

    /**
     * Replaces the method operator keyword from the given text and returns it.
     *
     * @param textWithOperator The text, with the method operator at the start
     * @return The text, without the method operator
     */
    public String removeOperatorFrom(String textWithOperator) {
        return textWithOperator.replaceFirst(getKeyword(), "");
    }

    /**
     * Validates the return type of the specific method operator, using the given parameters.
     *
     * @param method            The method, which should be validated
     * @param entityClass       The entity class of the validated repository
     * @param repositoryClass   THe repository class
     * @throws Exception if the validation is unsuccessful.
     */
    public void validateReturnType(Method method,
                                   Class<?> entityClass, Class<?> repositoryClass) throws Exception {
        returnTypeValidator.check(method, entityClass, repositoryClass);
    }

    /**
     * Parses the method operator by the name of the method. It just checks if the method is starting
     * with any method operator of the enumeration.
     *
     * @param methodNamePart The name of the method.
     * @return The MethodOperator if any is found, otherwise null.
     */
    public static MethodOperator parseMethodStartsWith(String methodNamePart) {
        for (MethodOperator operator : VALUES) {
            if (!methodNamePart.startsWith(operator.getKeyword())) {
                continue;
            }
            return operator;
        }
        return null;
    }
}
