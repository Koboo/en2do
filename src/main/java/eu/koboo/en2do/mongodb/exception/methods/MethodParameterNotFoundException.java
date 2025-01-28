package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodParameterNotFoundException extends RepositoryMethodException {

    public MethodParameterNotFoundException(Class<?> repositoryClass, Method method, int index, int totalParams) {
        super("Couldn't find parameter at index=" + index + ", because length=" + totalParams + "!",
            repositoryClass, method);
    }
}
