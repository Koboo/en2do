package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodParameterCountException extends RepositoryMethodException {

    public MethodParameterCountException(Class<?> repoClass, Method method, int expected) {
        super("Mismatching count of parameters: expected=" + expected + " but was actual=" + method.getParameterCount(),
            repoClass, method);
    }
}
