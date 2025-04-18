package eu.koboo.en2do.mongodb.exception.methods;


import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodPageRequiredException extends RepositoryMethodException {

    public MethodPageRequiredException(Class<?> repoClass, Method method) {
        super("Method requires at least one Pagination parameter in its signature!",
            repoClass, method);
    }
}
