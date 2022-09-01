package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class WrongFindMethodException extends Exception {

    public WrongFindMethodException(Method method, Class<?> entityClass) {
        super("Methods, which return a " + entityClass.getName() + " or a list with the entity, has to start with keyword \"findBy\"! (methodName=" + method.getName() + ")");
    }
}