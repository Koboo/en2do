package eu.koboo.en2do.repository.exception;

import java.lang.reflect.Method;

public class MethodInvalidSignatureException extends Exception {

    public MethodInvalidSignatureException(Method method, Class<?> entityClass) {
        super("Invalid method signature in " + method.getName() + " of " + entityClass.getName());
    }
}