package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class UnsupportedFilterException extends Exception {

    public UnsupportedFilterException(Method method, Class<?> entityClass) {
        super("Unsupported filter found on " + method.getName() + " in " + entityClass.getName() + "! " +
                "Please make sure to match the defined filter pattern!");
    }
}