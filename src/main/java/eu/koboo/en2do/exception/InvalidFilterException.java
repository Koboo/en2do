package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class InvalidFilterException extends Exception {

    public InvalidFilterException(Method method, Class<?> entityClass) {
        super("Invalid method filter in " + method.getName() + " of " + entityClass.getName());
    }
}