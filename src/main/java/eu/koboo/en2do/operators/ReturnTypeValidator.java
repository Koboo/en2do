package eu.koboo.en2do.operators;

import java.lang.reflect.Method;

/**
 * Represents the validation of the return type from a method.
 */
@FunctionalInterface
public interface ReturnTypeValidator {

    /**
     * Gets executed on the validation of the repository.
     *
     * @param method      The method, which should be validated
     * @param entityClass The class of the entity of the repository
     * @param repositoryClass   The class of the repository
     * @throws Exception if return type isn't valid
     */
    void check(Method method, Class<?> entityClass, Class<?> repositoryClass) throws Exception;
}
