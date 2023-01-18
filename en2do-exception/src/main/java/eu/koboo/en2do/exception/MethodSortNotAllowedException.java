package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MethodSortNotAllowedException extends Exception {

    public MethodSortNotAllowedException(Method method, Class<?> repoClass) {
        super("The method " + method.getName() + " in " +
                repoClass.getName() + " is not allowed to have any type of sorting.");
    }
}
