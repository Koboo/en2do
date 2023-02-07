package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodUnsupportedException extends Exception {

    public MethodUnsupportedException(Method method, Class<?> repoClass) {
        super("Method \"" + method.getName() + "\" of repository " + repoClass.getName() + " is not supported by en2do!");
    }
}
