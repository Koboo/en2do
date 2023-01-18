package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MethodInvalidSortLimitException extends Exception {

    public MethodInvalidSortLimitException(Method method, Class<?> repoClass) {
        super("Sorting limit size of less or equal \"0\" in method " + method.getName() + " in " +
                repoClass.getName() + " is not allowed.");
    }
}
