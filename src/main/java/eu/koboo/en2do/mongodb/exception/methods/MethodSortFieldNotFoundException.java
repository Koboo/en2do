package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodSortFieldNotFoundException extends RepositoryMethodException {

    public MethodSortFieldNotFoundException(Class<?> repoClass, Method method, String fieldName) {
        super("Couldn't find field \"" + fieldName + "\" specified by sorting annotation!",
            repoClass, method);
    }
}
