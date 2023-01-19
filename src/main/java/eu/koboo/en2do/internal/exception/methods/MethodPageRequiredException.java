package eu.koboo.en2do.internal.exception.methods;


import java.lang.reflect.Method;

public class MethodPageRequiredException extends Exception {

    public MethodPageRequiredException(Method method, Class<?> repoClass, Class<?> pagerClass) {
        super("The method " + method.getName() + " in " +
                repoClass.getName() + " requires at least a " + pagerClass.getName() + " object as parameter.");
    }
}
