package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MismatchingParameterException extends Exception {

    public MismatchingParameterException(Method method, Class<?> entityClass, Class<?> fieldClass, Class<?> paramClass) {
        super("Mismatching parameters in " + method.getName() + " of " + entityClass.getName() + "! " +
                "(field=" + fieldClass.getName() + ", param=" + paramClass.getName() + ")");
    }
}