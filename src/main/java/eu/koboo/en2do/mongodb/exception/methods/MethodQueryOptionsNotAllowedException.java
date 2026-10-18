package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodQueryOptionsNotAllowedException extends RepositoryMethodException {

    public MethodQueryOptionsNotAllowedException(Class<?> repoClass, Method method) {
        super("Method is not allowed to have any type of QueryOptions.",
            repoClass, method);
    }
}
