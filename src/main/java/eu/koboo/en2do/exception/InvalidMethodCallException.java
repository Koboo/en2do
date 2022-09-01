package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class InvalidMethodCallException extends Exception {

    public InvalidMethodCallException(Method method, Class<?> entityClass) {
        super("Invalid method call on " + method.getName() + " of " + entityClass.getName());
    }
}