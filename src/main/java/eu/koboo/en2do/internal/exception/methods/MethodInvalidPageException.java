package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodInvalidPageException extends Exception {

    public MethodInvalidPageException(Method method, Class<?> repoClass) {
        super("Page size of less or equal \"0\" in method " + method.getName() + " in " +
                repoClass.getName() + " is not allowed.");
    }
}
