package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodInvalidCallException extends RepositoryMethodException {

    public MethodInvalidCallException(Class<?> repositoryClass, Method method) {
        super("Invalid method call, because of missing operator!",
            repositoryClass, method);
    }
}
