package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodMismatchingTypeException extends RepositoryMethodException {

    public MethodMismatchingTypeException(Class<?> repositoryClass, Method method, Class<?> fieldClass, Class<?> paramClass) {
        super("The field in the entity is type \"" + fieldClass.getSimpleName() + "\" and the parameter type is " +
            "\"" + paramClass.getSimpleName() + "\". Mismatching parameter types!", repositoryClass, method);
    }
}
