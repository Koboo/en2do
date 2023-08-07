package eu.koboo.en2do.mongodb.exception.methods;

import java.lang.reflect.Method;

public class MethodSortNotAllowedException extends Exception {

    public MethodSortNotAllowedException(Method method, Class<?> repoClass) {
        super("The method \"" + method.getName() + "\" of " +
            repoClass.getName() + " is not allowed to have any type of sorting.");
    }
}
