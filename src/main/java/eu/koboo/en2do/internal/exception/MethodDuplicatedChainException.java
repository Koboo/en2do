package eu.koboo.en2do.internal.exception;

import java.lang.reflect.Method;

public class MethodDuplicatedChainException extends Exception {

    public MethodDuplicatedChainException(Method method, Class<?> entityClass) {
        super("Can't use \"And\" + \"Or\"  operation in one method, but exists in " +
                method.getName() + " of " + entityClass.getName());
    }
}
