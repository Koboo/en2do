package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MethodNoOperatorException extends Exception {

    public MethodNoOperatorException(Method method, Class<?> repoClass) {
        super("Couldn't find filter operator in " + method.getName() + " of " + repoClass.getName() + "!");
    }
}