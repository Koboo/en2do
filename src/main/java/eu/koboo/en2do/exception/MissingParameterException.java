package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MissingParameterException extends Exception {

    public MissingParameterException(Method method, Class<?> entityClass) {
        super("Missing object parameter found of " + method.getName() + " in " + entityClass.getName() + "!");
    }
}