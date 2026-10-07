package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodInvalidQueryLimitException extends RepositoryMethodException {

    public MethodInvalidQueryLimitException(Class<?> repoClass, Method method) {
        super("You're not allowed to use a sorting limit of less or equal to \"0\"!",
            repoClass, method);
    }
}
