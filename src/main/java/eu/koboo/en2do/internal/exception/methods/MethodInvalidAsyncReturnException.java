package eu.koboo.en2do.internal.exception.methods;

import eu.koboo.en2do.repository.methods.async.Async;

import java.lang.reflect.Method;

public class MethodInvalidAsyncReturnException extends Exception {

    public MethodInvalidAsyncReturnException(Method method, Class<?> repoClass) {
        super("Methods, which are annotated with " + Async.class + " have to return a CompletableFuture<T> with their " +
                " encapsulate return type as T. Invalid method is \"" + method.getName() + "\"" +
                " in repository " + repoClass.getName());
    }
}
