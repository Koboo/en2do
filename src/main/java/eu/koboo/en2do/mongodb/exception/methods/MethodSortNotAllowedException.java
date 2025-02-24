package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodSortNotAllowedException extends RepositoryMethodException {

    public MethodSortNotAllowedException(Class<?> repoClass, Method method) {
        super("Method is not allowed to have any type of sorting, because it is from type pagination!",
            repoClass, method);
    }
}
