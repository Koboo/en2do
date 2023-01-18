package eu.koboo.en2do.internal.exception;

import java.lang.reflect.Method;

public class RepositoryInvalidCallException extends Exception {

    public RepositoryInvalidCallException(Method method, Class<?> repoClass) {
        super("Invalid method call, because of missing operator in method " +
                method.getName() + " of repository " + repoClass.getName() + ".");
    }
}
