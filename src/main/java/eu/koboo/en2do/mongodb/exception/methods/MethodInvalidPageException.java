package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodInvalidPageException extends RepositoryMethodException {

    public MethodInvalidPageException(Class<?> repoClass, Method method) {
        super("You're not allowed to use a page size of less or equal to \"0\"!",
            repoClass, method);
    }
}
