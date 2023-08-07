package eu.koboo.en2do.mongodb.exception.methods;

import java.lang.reflect.Method;

public class MethodFieldNotFoundException extends Exception {

    public MethodFieldNotFoundException(String fieldName, Method method, Class<?> entityClass, Class<?> repoClass) {
        super("Couldn't find any field with the name \"" + fieldName + "\" in the entity " +
            entityClass.getName() + ". Used in method \"" + method.getName() + "\" in repository " + repoClass.getName() + ".");
    }
}
