package eu.koboo.en2do.mongodb.exception.methods;

import eu.koboo.en2do.repository.methods.async.Async;

import java.lang.reflect.Method;

public class MethodInvalidAsyncNameException extends Exception {

    public MethodInvalidAsyncNameException(Method method, Class<?> repoClass) {
        super("Methods, which start with the keyword \"async\" are not allowed in repository, except the predefined methods " +
            "of the \"AsyncRepository\" itself. If you want to create \"async\" methods, just annotate the method with @" + Async.class.getSimpleName() +
            " and encapsulate the return type \"T\" into a CompletableFuture<T>. Invalid method is \"" + method.getName() + "\"" +
            " in repository " + repoClass.getName() + ".");
    }
}
