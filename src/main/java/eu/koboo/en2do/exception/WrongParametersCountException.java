package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class WrongParametersCountException extends Exception {

    public WrongParametersCountException(Method method, Class<?> entityClass) {
        super("Wrong count of parameters in " + method.getName() + " of " + entityClass.getName() + "!");
    }
}