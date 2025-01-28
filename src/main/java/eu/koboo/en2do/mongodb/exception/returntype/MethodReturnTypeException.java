package eu.koboo.en2do.mongodb.exception.returntype;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodReturnTypeException extends RepositoryMethodException {

    public MethodReturnTypeException(Class<?> repositoryClass, Method method, String typeName) {
        super("This method is only allowed to return \"" + typeName + "\"!", repositoryClass, method);
    }
}
