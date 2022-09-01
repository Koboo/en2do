package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class NoFilterException extends Exception {

    public NoFilterException(Method method, Class<?> entityClass) {
        super("Couldn't find filter operator in " + method.getName() + " of " + entityClass.getName() + "!");
    }
}