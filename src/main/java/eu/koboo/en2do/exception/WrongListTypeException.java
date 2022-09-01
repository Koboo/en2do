package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class WrongListTypeException extends Exception {

    public WrongListTypeException(Method method, Class<?> entityClass, Class<?> listType) {
        super("Method " + method.getName() + " of " + entityClass.getName() + " returns a list with type " + listType.getName() + " but requires the entity!");
    }
}