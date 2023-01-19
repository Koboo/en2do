package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodFieldNotFoundException extends Exception {

    public MethodFieldNotFoundException(String fieldName, Method method, Class<?> entityClass, Class<?> repoClass) {
        super("Couldn't find field \"" + fieldName + "\" in " + entityClass.getName() + ". Used in method " +
                method.getName() + " of " + repoClass.getName());
    }
}
