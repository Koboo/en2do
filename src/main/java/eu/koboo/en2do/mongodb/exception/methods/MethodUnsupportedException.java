package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodUnsupportedException extends RepositoryMethodException {

    public MethodUnsupportedException(Class<?> repoClass, Method method) {
        super("Method is not supported by en2do!",
            repoClass, method);
    }
}
