package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodMixedSortException extends RepositoryMethodException {

    public MethodMixedSortException(Class<?> repoClass, Method method) {
        super("Mixed Sort object and annotation! That's not allowed.",
            repoClass, method);
    }
}
