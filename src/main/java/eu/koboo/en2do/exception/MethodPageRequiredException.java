package eu.koboo.en2do.exception;

import eu.koboo.en2do.repository.options.methods.paging.Pager;

import java.lang.reflect.Method;

public class MethodPageRequiredException extends Exception {

    public MethodPageRequiredException(Method method, Class<?> repoClass) {
        super("The method " + method.getName() + " in " +
                repoClass.getName() + " requires at least a " + Pager.class.getName() + " object as parameter.");
    }
}
