package eu.koboo.en2do.mongodb.exception.methods;

import java.lang.reflect.Method;

public class MethodInvalidCallException extends Exception {

    public MethodInvalidCallException(Method method, Class<?> repoClass) {
        super("Invalid method call, because of missing operator in method " +
            method.getName() + " of repository " + repoClass.getName() + ".");
    }
}
