package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodNoMethodOperatorException extends RepositoryMethodException {

    public MethodNoMethodOperatorException(Class<?> repositoryClass, Method method) {
        super("Couldn't find method operator!", repositoryClass, method);
    }
}
