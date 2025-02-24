package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodPageNotAllowedException extends RepositoryMethodException {

    public MethodPageNotAllowedException(Class<?> repoClass, Method method) {
        super("No pagination parameter in method signature found!",
            repoClass, method);
    }
}
