package eu.koboo.en2do.mongodb.exception.methods;


import java.lang.reflect.Method;

public class MethodPageRequiredException extends Exception {

    public MethodPageRequiredException(Method method, Class<?> repoClass, Class<?> paginationClass) {
        super("The method \"" + method.getName() + "\" in " +
            repoClass.getName() + " requires at least a " + paginationClass.getName() +
            " object as parameter.");
    }
}
