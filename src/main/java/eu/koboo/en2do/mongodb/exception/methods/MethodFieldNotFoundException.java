package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodFieldNotFoundException extends RepositoryMethodException {

    public MethodFieldNotFoundException(Class<?> repositoryClass, Method method, String fieldName) {
        super("Couldn't find any field with the name \"" + fieldName + "\"",
            repositoryClass, method);
    }
}
