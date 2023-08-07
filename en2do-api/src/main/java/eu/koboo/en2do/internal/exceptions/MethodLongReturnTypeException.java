package eu.koboo.en2do.internal.exceptions;

import java.lang.reflect.Method;

public class MethodLongReturnTypeException extends Exception {

    public MethodLongReturnTypeException(Method method, Class<?> repoClass) {
        super("Methods, which return " + Long.class.getName() + " has to start with keywords \"countBy\"! " +
            "Please correct the method \"" + method.getName() + "\" of repository " + repoClass.getName() + ".");
    }
}
