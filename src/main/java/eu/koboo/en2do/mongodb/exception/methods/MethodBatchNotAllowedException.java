package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class MethodBatchNotAllowedException extends RepositoryMethodException {

    public MethodBatchNotAllowedException(Class<?> repoClass, Method method) {
        super("Method is not allowed to use UpdateBatch parameter." +
                "It's only allowed in methods, which start with the operator \"updateFieldsBy\".",
            repoClass, method);
    }
}
