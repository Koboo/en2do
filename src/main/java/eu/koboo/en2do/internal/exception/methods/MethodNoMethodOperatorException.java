package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodNoMethodOperatorException extends Exception {

    public MethodNoMethodOperatorException(Method method, Class<?> repoClass) {
        super("Couldn't find method operator in \"" + method.getName() + "\" of " + repoClass.getName() + "!");
    }
}
