package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodDuplicatedChainException extends RepositoryMethodException {

    public MethodDuplicatedChainException(Class<?> repositoryClass, Method method) {
        super("It's not allowed to mix \"And\" + \"Or\" operation in one method.", repositoryClass, method);
    }
}
