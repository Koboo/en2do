package eu.koboo.en2do.repository.exception;

import java.lang.reflect.Method;

public class MethodNoFilterOperatorException extends Exception {

    public MethodNoFilterOperatorException(Method method, Class<?> repoClass) {
        super("Couldn't find filter operator in " + method.getName() + " of " + repoClass.getName() + "!");
    }
}