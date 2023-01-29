package eu.koboo.en2do.internal.exception.methods;

import eu.koboo.en2do.repository.methods.async.Async;

import java.lang.reflect.Method;

public class MethodInvalidAsyncNameException extends Exception {

    public MethodInvalidAsyncNameException(Method method, Class<?> repoClass) {
        super("Methods, which start with the keyword \"async\" are not allowed in repository, except the predefined methods " +
                "of the repository itself. If you want to create \"async\" methods, just annotate any method with " + Async.class +
                " and encapsulate the return type in a CompletableFuture<T>. Invalid method is \"" + method.getName() + "\"" +
                " in repository " + repoClass.getName());
    }
}
