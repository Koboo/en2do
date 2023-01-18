package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class RepositoryInvalidCallException extends Exception {

    public RepositoryInvalidCallException(Method method, Class<?> repoClass) {
        super("Invalid method call on " + method.getName() + " of " + repoClass.getName());
    }
}
