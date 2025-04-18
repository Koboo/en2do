package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodInvalidRegexParameterException extends RepositoryMethodException {

    public MethodInvalidRegexParameterException(Class<?> repositoryClass, Method method, Class<?> paramClass) {
        super("Invalid regex parameter found! Only regex parameters of type String or Pattern are allowed, " +
                "but used type is \"" + paramClass + "\".",
            repositoryClass, method);
    }
}
