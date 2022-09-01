package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class InvalidMethodOperationException extends Exception {

    public InvalidMethodOperationException(Method method, Class<?> entityClass) {
        super("Invalid method signature in " + method.getName() + " of " + entityClass.getName());
    }
}