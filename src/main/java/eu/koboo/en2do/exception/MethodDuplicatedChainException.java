package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MethodDuplicatedChainException extends Exception {

    public MethodDuplicatedChainException(Method method, Class<?> entityClass) {
        super("Can't use \"and\" and \"or\"  operation in one method, but exists in " +
                method.getName() + " of " + entityClass.getName());
    }
}
