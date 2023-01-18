package eu.koboo.en2do.internal.exception;

import java.lang.reflect.Method;

public class MethodInvalidListParameterException extends Exception {

    public MethodInvalidListParameterException(Method method, Class<?> repoClass, Class<?> fieldClass, Class<?> paramClass) {
        super("Invalid list parameter on " + method.getName() + " in " + repoClass.getName() + ", because " +
                "expected=" + fieldClass.getName() + ", param=" + paramClass + ".");
    }
}
