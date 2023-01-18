package eu.koboo.en2do.internal.exception;

import java.lang.reflect.Method;

public class MethodInvalidSortSkipException extends Exception {

    public MethodInvalidSortSkipException(Method method, Class<?> repoClass) {
        super("Sorting skip size of less or equal \"0\" in method " + method.getName() + " in " +
                repoClass.getName() + " is not allowed.");
    }
}
