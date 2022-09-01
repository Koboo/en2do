package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class WrongDeleteMethodException extends Exception {

    public WrongDeleteMethodException(Method method, Class<?> entityClass) {
        super("Methods, which return " + Boolean.class.getName() + " has to start with keyword \"deleteBy\"! (methodName=" + method.getName() + ", entity=" + entityClass.getName() + ")");
    }
}