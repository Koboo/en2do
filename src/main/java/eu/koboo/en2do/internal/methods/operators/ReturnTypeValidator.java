package eu.koboo.en2do.internal.methods.operators;

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
     * @param returnType  The return type, of the method
     * @param entityClass The class of the entity of the repository
     * @param repoClass   The class of the repository
     * @throws Exception if return type isn't valid
     */
    void check(Method method, Class<?> returnType, Class<?> entityClass, Class<?> repoClass) throws Exception;
}
