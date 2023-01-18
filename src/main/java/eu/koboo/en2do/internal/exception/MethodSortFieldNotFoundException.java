package eu.koboo.en2do.internal.exception;

import java.lang.reflect.Method;

public class MethodSortFieldNotFoundException extends Exception {

    public MethodSortFieldNotFoundException(String fieldName, Method method, Class<?> entityClass, Class<?> repoClass) {
        super("Couldn't find field \"" + fieldName + "\" specified by sorting in " + entityClass.getName() + ". " +
                "Used in method " + method.getName() + " of " + repoClass.getName());
    }
}
