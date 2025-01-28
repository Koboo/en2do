package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodInvalidListParameterException extends RepositoryMethodException {

    public MethodInvalidListParameterException(Class<?> repositoryClass, Method method, Class<?> fieldClass, Class<?> paramClass) {
        super("The List parameter of the filter method \"In\" uses invalid types! The List uses type \"" + paramClass.getName() + "\", " +
            "but expected a List using type \"" + fieldClass.getName() + "\".",
            repositoryClass, method);
    }
}
