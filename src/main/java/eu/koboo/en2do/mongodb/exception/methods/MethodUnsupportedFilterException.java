package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodUnsupportedFilterException extends RepositoryMethodException {

    public MethodUnsupportedFilterException(Class<?> repoClass, Method method) {
        super("Unsupported filter found! Please make sure to match the defined filter pattern!",
            repoClass, method);
    }
}
